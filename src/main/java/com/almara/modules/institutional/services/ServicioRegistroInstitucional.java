package com.almara.modules.institutional.services;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.institutional.dto.RegistroInstitucionalRespuesta;
import com.almara.modules.institutional.dto.RegistroInstitucionalSolicitud;
import com.almara.modules.institutional.dto.VerificacionCorreoRespuesta;
import com.almara.modules.institutional.dto.VerificacionCorreoSolicitud;
import com.almara.modules.institutional.models.AuditoriaInstitucional;
import com.almara.modules.institutional.models.ClienteInstitucional;
import com.almara.modules.institutional.models.EstadoCuentaInstitucional;
import com.almara.modules.institutional.models.SuscripcionInstitucional;
import com.almara.modules.institutional.ports.ServicioNotificacionPort;
import com.almara.modules.institutional.repositories.RepositorioAuditoriaInstitucional;
import com.almara.modules.institutional.repositories.RepositorioClienteInstitucional;
import com.almara.modules.institutional.repositories.RepositorioSuscripcionInstitucional;
import com.almara.modules.institutional.security.UtilidadPasswordHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Servicio de Aplicación para el Registro y Verificación de Clientes Institucionales (HU-16).
 */
@Service
public class ServicioRegistroInstitucional {

    private static final Logger log = LoggerFactory.getLogger(ServicioRegistroInstitucional.class);

    // Dominios genéricos gratuitos de consumo no permitidos para cuentas corporativas
    private static final List<String> DOMINIOS_PROHIBIDOS = Arrays.asList(
            "gmail.com", "hotmail.com", "outlook.com", "yahoo.com", "live.com", "icloud.com"
    );

    // Mínimo 8 caracteres, al menos 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial
    private static final Pattern PATRON_PASSWORD_ROBUSTO = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!*()_\\-\\[\\]{}|;:,.<>?/~`]).{8,255}$"
    );

    // ID del Plan Básico Gratuito sembrado en migración V5
    private static final UUID ID_PLAN_FREE = UUID.fromString("33333333-3333-3333-3333-333333333301");

    private final RepositorioClienteInstitucional repositorioClienteInstitucional;
    private final RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional;
    private final RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional;
    private final ServicioNotificacionPort servicioNotificacionPort;
    private final UtilidadPasswordHash utilidadPasswordHash;

    public ServicioRegistroInstitucional(RepositorioClienteInstitucional repositorioClienteInstitucional,
                                         RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional,
                                         RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional,
                                         ServicioNotificacionPort servicioNotificacionPort,
                                         UtilidadPasswordHash utilidadPasswordHash) {
        this.repositorioClienteInstitucional = repositorioClienteInstitucional;
        this.repositorioSuscripcionInstitucional = repositorioSuscripcionInstitucional;
        this.repositorioAuditoriaInstitucional = repositorioAuditoriaInstitucional;
        this.servicioNotificacionPort = servicioNotificacionPort;
        this.utilidadPasswordHash = utilidadPasswordHash;
    }

    @Transactional
    public RegistroInstitucionalRespuesta registrarCuenta(RegistroInstitucionalSolicitud solicitud) {
        validarCorreoCorporativo(solicitud.getCorreoCorporativo());
        validarRobustezPassword(solicitud.getPassword());

        String correoNormalizado = solicitud.getCorreoCorporativo().trim().toLowerCase();
        String nitLimpio = solicitud.getIdentificacionFiscalNit().trim();

        if (repositorioClienteInstitucional.existsByCorreoIgnoreCase(correoNormalizado)) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.CONFLICT,
                    "ENTIDAD_YA_REGISTRADA",
                    "El correo corporativo ya se encuentra registrado en la plataforma"
            );
        }

        if (repositorioClienteInstitucional.existsByIdentificacionFiscalNit(nitLimpio)) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.CONFLICT,
                    "ENTIDAD_YA_REGISTRADA",
                    "El NIT o documento fiscal ya se encuentra registrado en la plataforma"
            );
        }

        UUID idUsuario = UUID.randomUUID();
        String tokenVerificacion = UUID.randomUUID().toString();
        String passwordHash = utilidadPasswordHash.hashearPassword(solicitud.getPassword());

        ClienteInstitucional cliente = ClienteInstitucional.builder()
                .idUsuario(idUsuario)
                .correo(correoNormalizado)
                .passwordHash(passwordHash)
                .activo(true)
                .razonSocial(solicitud.getRazonSocial().trim())
                .identificacionFiscalNit(nitLimpio)
                .nombreContacto(solicitud.getNombreContacto().trim())
                .telefonoContacto(solicitud.getTelefonoContacto() != null ? solicitud.getTelefonoContacto().trim() : null)
                .sectorActividad(solicitud.getSectorActividad() != null ? solicitud.getSectorActividad().trim().toUpperCase() : "PRIVADO")
                .estadoCuenta(EstadoCuentaInstitucional.PENDIENTE_APROBACION.name())
                .correoVerificado(false)
                .tokenVerificacion(tokenVerificacion)
                .rolNombre("ROLE_INSTITUTIONAL")
                .fechaRegistro(Instant.now())
                .build();

        repositorioClienteInstitucional.save(cliente);

        // Envío de correo de verificación (<<include>>)
        servicioNotificacionPort.enviarCorreoVerificacion(
                cliente.getCorreo(),
                cliente.getRazonSocial(),
                tokenVerificacion
        );

        // Auditoría inmutable (RNF-13)
        AuditoriaInstitucional auditoria = AuditoriaInstitucional.builder()
                .idAuditoria(UUID.randomUUID())
                .idEntidadAfectada(cliente.getIdUsuario())
                .tipoEvento("REGISTRO_INSTITUCIONAL")
                .detalle("Registro de nueva entidad " + cliente.getRazonSocial() + " con NIT " + cliente.getIdentificacionFiscalNit())
                .actorResponsable(cliente.getCorreo())
                .fechaEvento(Instant.now())
                .build();
        repositorioAuditoriaInstitucional.save(auditoria);

        log.info("Entidad registrada con éxito: id={}, NIT={}, correo={}", cliente.getIdUsuario(), nitLimpio, correoNormalizado);

        return RegistroInstitucionalRespuesta.builder()
                .idInstitucion(cliente.getIdUsuario())
                .razonSocial(cliente.getRazonSocial())
                .correoCorporativo(cliente.getCorreo())
                .estadoCuenta(cliente.getEstadoCuenta())
                .correoVerificado(cliente.isCorreoVerificado())
                .tokenVerificacionSimulado(tokenVerificacion)
                .mensaje("Cuenta institucional registrada exitosamente. Se ha enviado un enlace de verificación a su correo corporativo.")
                .build();
    }

    @Transactional
    public VerificacionCorreoRespuesta verificarCorreoYActivarCuenta(VerificacionCorreoSolicitud solicitud) {
        if (solicitud == null || solicitud.getTokenVerificacion() == null || solicitud.getTokenVerificacion().isBlank()) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "TOKEN_VERIFICACION_INVALIDO",
                    "El token de verificación no puede estar vacío"
            );
        }

        String token = solicitud.getTokenVerificacion().trim();
        ClienteInstitucional cliente = repositorioClienteInstitucional.findByTokenVerificacion(token)
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.BAD_REQUEST,
                        "TOKEN_VERIFICACION_INVALIDO",
                        "El token de verificación es inválido, ha expirado o ya fue utilizado"
                ));

        cliente.activarPorVerificacionCorreo();
        repositorioClienteInstitucional.save(cliente);

        // Asignación automática de suscripción inicial gratuita (PLAN_FREE con 30 días de vigencia)
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plusSeconds(30L * 86400L);
        UUID idSuscripcion = UUID.randomUUID();

        SuscripcionInstitucional suscripcion = SuscripcionInstitucional.builder()
                .idSuscripcion(idSuscripcion)
                .idInstitucion(cliente.getIdUsuario())
                .idPlan(ID_PLAN_FREE)
                .estadoSuscripcion("ACTIVA")
                .fechaInicio(ahora)
                .fechaExpiracion(expiracion)
                .fechaActualizacion(ahora)
                .build();

        repositorioSuscripcionInstitucional.save(suscripcion);

        // Notificación de confirmación de activación
        servicioNotificacionPort.enviarCorreoConfirmacion(
                cliente.getCorreo(),
                "Almara — Cuenta Institucional Activada",
                "Estimado representante de " + cliente.getRazonSocial() + ",\n\n"
                        + "Su correo ha sido verificado satisfactoriamente. Su cuenta ahora está ACTIVA con el Plan Institucional Básico.\n"
                        + "Ya puede iniciar sesión para acceder al panel analítico institucional."
        );

        // Auditoría inmutable (RNF-13)
        AuditoriaInstitucional auditoria = AuditoriaInstitucional.builder()
                .idAuditoria(UUID.randomUUID())
                .idEntidadAfectada(cliente.getIdUsuario())
                .tipoEvento("VERIFICACION_CORREO_Y_ACTIVACION")
                .detalle("Activación de cuenta institucional y asignación de PLAN_FREE por verificación de correo exitosa")
                .actorResponsable(cliente.getCorreo())
                .fechaEvento(Instant.now())
                .build();
        repositorioAuditoriaInstitucional.save(auditoria);

        log.info("Cuenta institucional activada con éxito: id={}, razonSocial={}", cliente.getIdUsuario(), cliente.getRazonSocial());

        return VerificacionCorreoRespuesta.builder()
                .idInstitucion(cliente.getIdUsuario())
                .razonSocial(cliente.getRazonSocial())
                .correoCorporativo(cliente.getCorreo())
                .estadoCuenta(cliente.getEstadoCuenta())
                .correoVerificado(cliente.isCorreoVerificado())
                .idSuscripcion(idSuscripcion)
                .codigoPlan("PLAN_FREE")
                .fechaExpiracionSuscripcion(expiracion)
                .mensaje("Correo verificado y cuenta activada exitosamente. Se ha habilitado su plan institucional base.")
                .build();
    }

    private void validarCorreoCorporativo(String correo) {
        if (correo == null || !correo.contains("@")) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "CORREO_NO_CORPORATIVO",
                    "El formato del correo corporativo no es válido"
            );
        }
        String dominio = correo.substring(correo.lastIndexOf('@') + 1).trim().toLowerCase();
        if (DOMINIOS_PROHIBIDOS.contains(dominio)) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "CORREO_NO_CORPORATIVO",
                    "No se permiten correos de dominios públicos gratuitos (" + dominio + "). Utilice un correo con dominio corporativo o gubernamental."
            );
        }
    }

    private void validarRobustezPassword(String password) {
        if (password == null || !PATRON_PASSWORD_ROBUSTO.matcher(password).matches()) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "PASSWORD_NO_SEGURO",
                    "La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, minúsculas, números y al menos un carácter especial."
            );
        }
    }
}

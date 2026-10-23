package com.almara.modules.institutional.services;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.institutional.dto.LoginInstitucionalRespuesta;
import com.almara.modules.institutional.dto.LoginInstitucionalSolicitud;
import com.almara.modules.institutional.dto.LogoutInstitucionalRespuesta;
import com.almara.modules.institutional.dto.SesionActualRespuesta;
import com.almara.modules.institutional.models.AuditoriaInstitucional;
import com.almara.modules.institutional.models.ClienteInstitucional;
import com.almara.modules.institutional.models.EstadoCuentaInstitucional;
import com.almara.modules.institutional.models.MatrizPermisoModulo;
import com.almara.modules.institutional.models.ModuloSistema;
import com.almara.modules.institutional.models.PlanSuscripcion;
import com.almara.modules.institutional.models.SuscripcionInstitucional;
import com.almara.modules.institutional.repositories.RepositorioAuditoriaInstitucional;
import com.almara.modules.institutional.repositories.RepositorioClienteInstitucional;
import com.almara.modules.institutional.repositories.RepositorioMatrizPermisoModulo;
import com.almara.modules.institutional.repositories.RepositorioModuloSistema;
import com.almara.modules.institutional.repositories.RepositorioPlanSuscripcion;
import com.almara.modules.institutional.repositories.RepositorioSuscripcionInstitucional;
import com.almara.modules.institutional.security.ClaimsTokenInstitucional;
import com.almara.modules.institutional.security.ProveedorTokenJwtInstitucional;
import com.almara.modules.institutional.security.ServicioRevocacionToken;
import com.almara.modules.institutional.security.UtilidadPasswordHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de Autenticación para Clientes Institucionales (HU-17).
 */
@Service
public class ServicioAutenticacionInstitucional {

    private static final Logger log = LoggerFactory.getLogger(ServicioAutenticacionInstitucional.class);

    private final RepositorioClienteInstitucional repositorioClienteInstitucional;
    private final RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional;
    private final RepositorioPlanSuscripcion repositorioPlanSuscripcion;
    private final RepositorioMatrizPermisoModulo repositorioMatrizPermisoModulo;
    private final RepositorioModuloSistema repositorioModuloSistema;
    private final RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional;
    private final UtilidadPasswordHash utilidadPasswordHash;
    private final ProveedorTokenJwtInstitucional proveedorTokenJwtInstitucional;
    private final ServicioRevocacionToken servicioRevocacionToken;

    public ServicioAutenticacionInstitucional(
            RepositorioClienteInstitucional repositorioClienteInstitucional,
            RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional,
            RepositorioPlanSuscripcion repositorioPlanSuscripcion,
            RepositorioMatrizPermisoModulo repositorioMatrizPermisoModulo,
            RepositorioModuloSistema repositorioModuloSistema,
            RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional,
            UtilidadPasswordHash utilidadPasswordHash,
            ProveedorTokenJwtInstitucional proveedorTokenJwtInstitucional,
            ServicioRevocacionToken servicioRevocacionToken) {
        this.repositorioClienteInstitucional = repositorioClienteInstitucional;
        this.repositorioSuscripcionInstitucional = repositorioSuscripcionInstitucional;
        this.repositorioPlanSuscripcion = repositorioPlanSuscripcion;
        this.repositorioMatrizPermisoModulo = repositorioMatrizPermisoModulo;
        this.repositorioModuloSistema = repositorioModuloSistema;
        this.repositorioAuditoriaInstitucional = repositorioAuditoriaInstitucional;
        this.utilidadPasswordHash = utilidadPasswordHash;
        this.proveedorTokenJwtInstitucional = proveedorTokenJwtInstitucional;
        this.servicioRevocacionToken = servicioRevocacionToken;
    }

    @Transactional
    public LoginInstitucionalRespuesta iniciarSesion(LoginInstitucionalSolicitud solicitud) {
        if (solicitud == null || solicitud.getCorreoInstitucional() == null || solicitud.getPassword() == null) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.UNAUTHORIZED,
                    "CREDENCIALES_INVALIDAS",
                    "Correo institucional o contraseña incorrectos"
            );
        }

        String correoLimpio = solicitud.getCorreoInstitucional().trim().toLowerCase();

        ClienteInstitucional cliente = repositorioClienteInstitucional.findByCorreoIgnoreCase(correoLimpio)
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.UNAUTHORIZED,
                        "CREDENCIALES_INVALIDAS",
                        "Correo institucional o contraseña incorrectos"
                ));

        if (!utilidadPasswordHash.verificar(solicitud.getPassword(), cliente.getPasswordHash())) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.UNAUTHORIZED,
                    "CREDENCIALES_INVALIDAS",
                    "Correo institucional o contraseña incorrectos"
            );
        }

        if (!cliente.isCorreoVerificado() || EstadoCuentaInstitucional.PENDIENTE_APROBACION.name().equalsIgnoreCase(cliente.getEstadoCuenta())) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.FORBIDDEN,
                    "CUENTA_PENDIENTE_VERIFICACION",
                    "Su cuenta se encuentra pendiente de verificación de correo electrónico. Verifique su correo para ingresar."
            );
        }

        if (EstadoCuentaInstitucional.SUSPENDIDA.name().equalsIgnoreCase(cliente.getEstadoCuenta())
                || EstadoCuentaInstitucional.INACTIVA.name().equalsIgnoreCase(cliente.getEstadoCuenta())
                || !cliente.isActivo()) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.FORBIDDEN,
                    "CUENTA_SUSPENDIDA_O_INACTIVA",
                    "Su cuenta institucional se encuentra suspendida o inactiva. Comuníquese con la administración de Almara."
            );
        }

        // Consultar estado de suscripción y evaluar vigencia en tiempo real
        Optional<SuscripcionInstitucional> suscripcionOpt =
                repositorioSuscripcionInstitucional.findFirstByIdInstitucionOrderByFechaExpiracionDesc(cliente.getIdUsuario());

        String estadoSuscripcion = "SIN_SUSCRIPCION";
        String codigoPlan = "PLAN_FREE";
        String nombrePlan = "Plan Institucional Básico";
        boolean requiereRenovacion = false;
        String rutaRedireccion = "/institutional/dashboard";
        List<String> modulosHabilitados = new ArrayList<>();

        if (suscripcionOpt.isPresent()) {
            SuscripcionInstitucional suscripcion = suscripcionOpt.get();

            if ("ACTIVA".equalsIgnoreCase(suscripcion.getEstadoSuscripcion()) && !suscripcion.estaVigente()) {
                suscripcion.revocarPorExpiracion();
                repositorioSuscripcionInstitucional.save(suscripcion);
            }

            estadoSuscripcion = suscripcion.getEstadoSuscripcion();

            Optional<PlanSuscripcion> planOpt = repositorioPlanSuscripcion.findById(suscripcion.getIdPlan());
            if (planOpt.isPresent()) {
                codigoPlan = planOpt.get().getCodigo();
                nombrePlan = planOpt.get().getNombre();
            }

            if (!suscripcion.estaVigente()) {
                requiereRenovacion = true;
                rutaRedireccion = "/institutional/planes/renovacion";
            } else {
                List<MatrizPermisoModulo> permisos = repositorioMatrizPermisoModulo.findByIdPlanAndModuloHabilitadoTrue(suscripcion.getIdPlan());
                for (MatrizPermisoModulo permiso : permisos) {
                    Optional<ModuloSistema> modOpt = repositorioModuloSistema.findById(permiso.getIdModulo());
                    modOpt.ifPresent(m -> modulosHabilitados.add(m.getCodigoModulo()));
                }
            }
        } else {
            requiereRenovacion = true;
            rutaRedireccion = "/institutional/planes/renovacion";
        }

        String tokenJwt = proveedorTokenJwtInstitucional.generarToken(cliente, codigoPlan, estadoSuscripcion);

        // Registro de auditoría (RNF-13)
        AuditoriaInstitucional auditoria = AuditoriaInstitucional.builder()
                .idAuditoria(UUID.randomUUID())
                .idEntidadAfectada(cliente.getIdUsuario())
                .tipoEvento("LOGIN_INSTITUCIONAL")
                .detalle("Inicio de sesión exitoso de la entidad " + cliente.getRazonSocial() + " con plan " + codigoPlan)
                .actorResponsable(cliente.getCorreo())
                .fechaEvento(Instant.now())
                .build();
        repositorioAuditoriaInstitucional.save(auditoria);

        log.info("Login exitoso para cliente institucional: id={}, correo={}, plan={}, requiereRenovacion={}",
                cliente.getIdUsuario(), cliente.getCorreo(), codigoPlan, requiereRenovacion);

        return LoginInstitucionalRespuesta.builder()
                .tokenAcceso(tokenJwt)
                .tipoToken("Bearer")
                .expiraEnSegundos(proveedorTokenJwtInstitucional.getTiempoExpiracionSegundos())
                .idInstitucion(cliente.getIdUsuario())
                .razonSocial(cliente.getRazonSocial())
                .correoInstitucional(cliente.getCorreo())
                .rol(cliente.getRolNombre())
                .codigoPlan(codigoPlan)
                .nombrePlan(nombrePlan)
                .estadoSuscripcion(estadoSuscripcion)
                .modulosHabilitados(modulosHabilitados)
                .requiereRenovacion(requiereRenovacion)
                .rutaRedireccion(rutaRedireccion)
                .build();
    }

    /**
     * Cierra la sesión activa del cliente institucional y revoca inmediatamente el token JWT (HU-18).
     */
    @Transactional
    public LogoutInstitucionalRespuesta cerrarSesion(String encabezadoAuthorization) {
        String token = extraerTokenBearer(encabezadoAuthorization);

        ClaimsTokenInstitucional claims = proveedorTokenJwtInstitucional.validarYExtraerClaims(token);

        // Revocar token y jti en blacklist síncrona
        Instant fechaExp = Instant.ofEpochSecond(claims.getExp());
        servicioRevocacionToken.revocarToken(token, fechaExp);
        if (claims.getJti() != null && !claims.getJti().isBlank()) {
            servicioRevocacionToken.revocarToken(claims.getJti(), fechaExp);
        }

        // Ejecutar método de dominio
        repositorioClienteInstitucional.findById(claims.getIdUsuario())
                .ifPresent(cliente -> cliente.cerrarSesion(token));

        // Registro de auditoría (RNF-13)
        AuditoriaInstitucional auditoria = AuditoriaInstitucional.builder()
                .idAuditoria(UUID.randomUUID())
                .idEntidadAfectada(claims.getIdUsuario())
                .tipoEvento("LOGOUT_INSTITUCIONAL")
                .detalle("Cierre de sesión y revocación síncrona de token para la entidad " + claims.getRazonSocial())
                .actorResponsable(claims.getCorreo())
                .fechaEvento(Instant.now())
                .build();
        repositorioAuditoriaInstitucional.save(auditoria);

        log.info("Sesión cerrada y token revocado para cliente institucional: id={}, correo={}",
                claims.getIdUsuario(), claims.getCorreo());

        return LogoutInstitucionalRespuesta.builder()
                .mensaje("Sesión cerrada exitosamente y token de acceso revocado")
                .tokenRevocado(true)
                .marcaTemporal(Instant.now())
                .build();
    }

    /**
     * Valida y retorna los datos de la sesión activa del cliente institucional autenticado (HU-18).
     */
    @Transactional(readOnly = true)
    public SesionActualRespuesta obtenerSesionActual(String encabezadoAuthorization) {
        String token = extraerTokenBearer(encabezadoAuthorization);
        ClaimsTokenInstitucional claims = proveedorTokenJwtInstitucional.validarYExtraerClaims(token);

        long segundosRestantes = Math.max(0, claims.getExp() - Instant.now().getEpochSecond());

        return SesionActualRespuesta.builder()
                .idInstitucion(claims.getIdUsuario())
                .correoInstitucional(claims.getCorreo())
                .razonSocial(claims.getRazonSocial())
                .rol(claims.getRol())
                .codigoPlan(claims.getCodigoPlan())
                .estadoSuscripcion(claims.getEstadoSuscripcion())
                .expiraEnSegundosRestantes(segundosRestantes)
                .activo(true)
                .build();
    }

    private String extraerTokenBearer(String encabezadoAuthorization) {
        if (encabezadoAuthorization == null || !encabezadoAuthorization.startsWith("Bearer ")) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.UNAUTHORIZED,
                    "SESION_INVALIDA_O_REVOCADA",
                    "Debe proporcionar una cabecera Authorization Bearer válida"
            );
        }
        String token = encabezadoAuthorization.substring(7).trim();
        if (token.isEmpty()) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.UNAUTHORIZED,
                    "SESION_INVALIDA_O_REVOCADA",
                    "El token de autorización no puede estar vacío"
            );
        }
        return token;
    }
}

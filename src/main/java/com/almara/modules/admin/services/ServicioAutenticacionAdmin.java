package com.almara.modules.admin.services;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.admin.dto.LoginAdminRespuesta;
import com.almara.modules.admin.dto.LoginAdminSolicitud;
import com.almara.modules.admin.dto.LogoutAdminRespuesta;
import com.almara.modules.admin.dto.SesionAdminRespuesta;
import com.almara.modules.admin.models.Administrador;
import com.almara.modules.admin.repositories.RepositorioAdministrador;
import com.almara.modules.admin.security.ClaimsTokenAdmin;
import com.almara.modules.admin.security.EstrategiaPasswordHash;
import com.almara.modules.admin.security.ProveedorTokenJwtAdmin;
import com.almara.modules.institutional.security.ServicioRevocacionToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio central de autenticación y seguridad para el Módulo de Administración (HU-11, HU-18).
 * Orquesta la validación de credenciales (`<<include>> Validar credenciales`),
 * la emisión de tokens JWT seguros y el registro en la bitácora de auditoría.
 */
@Service
public class ServicioAutenticacionAdmin {

    private static final Logger log = LoggerFactory.getLogger(ServicioAutenticacionAdmin.class);

    private final RepositorioAdministrador repositorioAdministrador;
    private final EstrategiaPasswordHash estrategiaPasswordHash;
    private final ProveedorTokenJwtAdmin proveedorTokenJwtAdmin;
    private final ServicioRevocacionToken servicioRevocacionToken;
    private final ServicioAuditoriaAdmin servicioAuditoriaAdmin;

    private final int maxIntentosFallidos;
    private final int minutosBloqueoTemporal;

    public ServicioAutenticacionAdmin(
            RepositorioAdministrador repositorioAdministrador,
            EstrategiaPasswordHash estrategiaPasswordHash,
            ProveedorTokenJwtAdmin proveedorTokenJwtAdmin,
            ServicioRevocacionToken servicioRevocacionToken,
            ServicioAuditoriaAdmin servicioAuditoriaAdmin,
            @Value("${almara.seguridad.admin-max-intentos:5}") int maxIntentosFallidos,
            @Value("${almara.seguridad.admin-minutos-bloqueo:15}") int minutosBloqueoTemporal) {
        this.repositorioAdministrador = repositorioAdministrador;
        this.estrategiaPasswordHash = estrategiaPasswordHash;
        this.proveedorTokenJwtAdmin = proveedorTokenJwtAdmin;
        this.servicioRevocacionToken = servicioRevocacionToken;
        this.servicioAuditoriaAdmin = servicioAuditoriaAdmin;
        this.maxIntentosFallidos = maxIntentosFallidos;
        this.minutosBloqueoTemporal = minutosBloqueoTemporal;
    }

    /**
     * Inicia sesión como administrador aplicando el caso incluido `<<include>> Validar credenciales` (HU-11).
     *
     * @param solicitud Credenciales ingresadas (usuario/correo y contraseña plana)
     * @param ipOrigen  Dirección IP del cliente para trazabilidad
     * @return DTO con token de sesión firmado criptográficamente
     */
    @Transactional(noRollbackFor = ExcepcionReglaNegocioInstitucional.class)
    public LoginAdminRespuesta iniciarSesion(LoginAdminSolicitud solicitud, String ipOrigen) {
        if (solicitud == null || solicitud.getUsuarioAdmin() == null || solicitud.getPasswordAdmin() == null) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST, "DATOS_REQUERIDOS", "El usuario y la contraseña son campos obligatorios");
        }

        String identificador = solicitud.getUsuarioAdmin().trim();
        String passwordPlano = solicitud.getPasswordAdmin();

        // 1. Inclusión <<include>> Validar credenciales
        Administrador admin = validarCredenciales(identificador, passwordPlano, ipOrigen);

        // 2. Generar token de sesión JWT con rol ROLE_ADMIN
        String token = proveedorTokenJwtAdmin.generarToken(admin);

        // 3. Auditoría de inicio de sesión exitoso
        servicioAuditoriaAdmin.registrarEvento(
                admin.getIdUsuario(),
                "LOGIN_ADMIN_EXITOSO",
                "Inicio de sesión administrativo exitoso para " + admin.getCorreo(),
                admin.getCorreo(),
                ipOrigen
        );

        return new LoginAdminRespuesta(
                token,
                admin.getIdUsuario(),
                admin.getNombreCompleto(),
                admin.getCorreo(),
                admin.getRolNombre(),
                proveedorTokenJwtAdmin.getTiempoExpiracionSegundos()
        );
    }

    /**
     * Caso de uso incluido obligatorio: `<<include>> Validar credenciales` (HU-11).
     * Comprueba existencia de cuenta, hashing seguro (BCrypt/PBKDF2), estado activo y bloqueo por intentos fallidos.
     */
    @Transactional(noRollbackFor = ExcepcionReglaNegocioInstitucional.class)
    public Administrador validarCredenciales(String identificador, String passwordPlano, String ipOrigen) {
        Optional<Administrador> adminOpt = repositorioAdministrador.findByCorreoIgnoreCase(identificador);

        if (adminOpt.isEmpty()) {
            // Mitigación de timing attack calculando verificación simulada
            estrategiaPasswordHash.verificar(passwordPlano, "65536:dummySalt12345678:dummyHash12345678");
            servicioAuditoriaAdmin.registrarEvento(
                    null,
                    "LOGIN_ADMIN_FALLIDO",
                    "Intento de inicio de sesión fallido para cuenta inexistente: " + identificador,
                    identificador,
                    ipOrigen
            );
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", "Credenciales administrativas incorrectas o cuenta no autorizada");
        }

        Administrador admin = adminOpt.get();

        // Validación de cuenta activa
        if (!admin.isActivo()) {
            servicioAuditoriaAdmin.registrarEvento(
                    admin.getIdUsuario(),
                    "LOGIN_ADMIN_INACTIVO",
                    "Intento de acceso a cuenta administrativa desactivada: " + admin.getCorreo(),
                    admin.getCorreo(),
                    ipOrigen
            );
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.FORBIDDEN, "CUENTA_ADMIN_INACTIVA", "La cuenta administrativa se encuentra desactivada temporalmente");
        }

        // Validación de bloqueo temporal por intentos fallidos excesivos
        if (admin.estaBloqueado()) {
            long minutosRestantes = Duration.between(LocalDateTime.now(), admin.getBloqueadoHasta()).toMinutes() + 1;
            servicioAuditoriaAdmin.registrarEvento(
                    admin.getIdUsuario(),
                    "LOGIN_ADMIN_BLOQUEADO",
                    "Intento de acceso a cuenta bloqueada temporalmente hasta " + admin.getBloqueadoHasta(),
                    admin.getCorreo(),
                    ipOrigen
            );
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.LOCKED,
                    "CUENTA_BLOQUEADA_TEMPORALMENTE",
                    "La cuenta administrativa ha sido bloqueada temporalmente por seguridad tras múltiples intentos fallidos. Intente nuevamente en " + minutosRestantes + " minuto(s).");
        }

        // Validación de contraseña mediante Strategy Pattern
        boolean passwordValido = estrategiaPasswordHash.verificar(passwordPlano, admin.getPasswordHash());

        if (!passwordValido) {
            boolean quedoBloqueado = admin.registrarIntentoFallido(maxIntentosFallidos, minutosBloqueoTemporal);
            repositorioAdministrador.saveAndFlush(admin);

            if (quedoBloqueado) {
                servicioAuditoriaAdmin.registrarEvento(
                        admin.getIdUsuario(),
                        "ADMIN_BLOQUEADO_TEMPORALMENTE",
                        "Cuenta bloqueada por " + minutosBloqueoTemporal + " minutos tras " + admin.getIntentosFallidos() + " intentos fallidos",
                        admin.getCorreo(),
                        ipOrigen
                );
                throw new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.LOCKED,
                        "CUENTA_BLOQUEADA_TEMPORALMENTE",
                        "Ha superado el límite de intentos fallidos. Su cuenta ha sido bloqueada temporalmente por " + minutosBloqueoTemporal + " minutos.");
            } else {
                servicioAuditoriaAdmin.registrarEvento(
                        admin.getIdUsuario(),
                        "LOGIN_ADMIN_FALLIDO",
                        "Contraseña incorrecta. Intento " + admin.getIntentosFallidos() + " de " + maxIntentosFallidos,
                        admin.getCorreo(),
                        ipOrigen
                );
                throw new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", "Credenciales administrativas incorrectas o cuenta no autorizada");
            }
        }

        // Credenciales correctas: registrar login y resetear intentos
        admin.registrarLoginExitoso();
        repositorioAdministrador.saveAndFlush(admin);

        return admin;
    }

    /**
     * Consulta y valida el estado de la sesión administrativa actual (HU-11, HU-18).
     */
    @Transactional(readOnly = true)
    public SesionAdminRespuesta obtenerSesionActual(String authorizationHeader) {
        String token = extraerTokenBearer(authorizationHeader);
        ClaimsTokenAdmin claims = proveedorTokenJwtAdmin.validarYExtraerClaims(token);

        Administrador admin = repositorioAdministrador.findById(claims.getIdAdmin())
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.UNAUTHORIZED, "ADMIN_NO_ENCONTRADO", "El usuario administrativo no existe en la plataforma"));

        if (!admin.isActivo() || admin.estaBloqueado()) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.FORBIDDEN, "SESION_ADMIN_INVALIDA", "La cuenta administrativa no se encuentra activa");
        }

        long ahoraSegundos = Instant.now().getEpochSecond();
        long segundosRestantes = Math.max(0, claims.getExp() - ahoraSegundos);

        return new SesionAdminRespuesta(
                true,
                admin.getIdUsuario(),
                admin.getNombreCompleto(),
                admin.getCorreo(),
                admin.getRolNombre(),
                segundosRestantes
        );
    }

    /**
     * Cierra la sesión administrativa e invalida el token de acceso JWT actual (HU-18: include Revocar Tokens y Sesiones Activas).
     */
    public LogoutAdminRespuesta cerrarSesion(String authorizationHeader, String ipOrigen) {
        String token = extraerTokenBearer(authorizationHeader);
        ClaimsTokenAdmin claims = proveedorTokenJwtAdmin.validarYExtraerClaims(token);

        Instant expiracion = Instant.ofEpochSecond(claims.getExp());

        // Inclusión <<include>> Revocar Tokens y Sesiones Activas
        servicioRevocacionToken.revocarToken(token, expiracion);
        if (claims.getJti() != null) {
            servicioRevocacionToken.revocarToken(claims.getJti(), expiracion);
        }

        servicioAuditoriaAdmin.registrarEvento(
                claims.getIdAdmin(),
                "LOGOUT_ADMIN",
                "Cierre de sesión administrativo seguro y revocación de token activo (HU-18)",
                claims.getCorreo(),
                ipOrigen
        );

        return new LogoutAdminRespuesta(true, "Sesión administrativa cerrada exitosamente. Token revocado.");
    }

    private String extraerTokenBearer(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.UNAUTHORIZED, "CABECERA_AUTORIZACION_FALTANTE", "Se requiere una cabecera Authorization con prefijo 'Bearer '");
        }
        return authorizationHeader.substring(7).trim();
    }
}

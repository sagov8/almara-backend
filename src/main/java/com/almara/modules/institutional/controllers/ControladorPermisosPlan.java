package com.almara.modules.institutional.controllers;

import com.almara.modules.institutional.dto.MatrizPermisosRespuesta;
import com.almara.modules.institutional.dto.ModuloCatalogoDto;
import com.almara.modules.institutional.dto.VerificarModuloRespuesta;
import com.almara.modules.institutional.models.MatrizPermisoModulo;
import com.almara.modules.institutional.models.ModuloSistema;
import com.almara.modules.institutional.models.PlanSuscripcion;
import com.almara.modules.institutional.models.SuscripcionInstitucional;
import com.almara.modules.institutional.repositories.RepositorioMatrizPermisoModulo;
import com.almara.modules.institutional.repositories.RepositorioModuloSistema;
import com.almara.modules.institutional.repositories.RepositorioPlanSuscripcion;
import com.almara.modules.institutional.repositories.RepositorioSuscripcionInstitucional;
import com.almara.modules.institutional.security.ClaimsTokenInstitucional;
import com.almara.modules.institutional.security.ExcepcionModuloRestringidoPorPlan;
import com.almara.modules.institutional.security.ProveedorTokenJwtInstitucional;
import com.almara.modules.institutional.security.ResultadoValidacionModulo;
import com.almara.modules.institutional.security.ValidadorPermisosBackend;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador REST para la consulta de la Matriz de Permisos y Validación de Acceso a Módulos (HU-20).
 */
@RestController
@RequestMapping("/api/v1/institutional/permisos")
@CrossOrigin(origins = "*")
public class ControladorPermisosPlan {

    private final ValidadorPermisosBackend validadorPermisosBackend;
    private final ProveedorTokenJwtInstitucional proveedorTokenJwtInstitucional;
    private final RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional;
    private final RepositorioPlanSuscripcion repositorioPlanSuscripcion;
    private final RepositorioModuloSistema repositorioModuloSistema;
    private final RepositorioMatrizPermisoModulo repositorioMatrizPermisoModulo;

    public ControladorPermisosPlan(ValidadorPermisosBackend validadorPermisosBackend,
                                   ProveedorTokenJwtInstitucional proveedorTokenJwtInstitucional,
                                   RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional,
                                   RepositorioPlanSuscripcion repositorioPlanSuscripcion,
                                   RepositorioModuloSistema repositorioModuloSistema,
                                   RepositorioMatrizPermisoModulo repositorioMatrizPermisoModulo) {
        this.validadorPermisosBackend = validadorPermisosBackend;
        this.proveedorTokenJwtInstitucional = proveedorTokenJwtInstitucional;
        this.repositorioSuscripcionInstitucional = repositorioSuscripcionInstitucional;
        this.repositorioPlanSuscripcion = repositorioPlanSuscripcion;
        this.repositorioModuloSistema = repositorioModuloSistema;
        this.repositorioMatrizPermisoModulo = repositorioMatrizPermisoModulo;
    }

    /**
     * Valida si el cliente tiene acceso al módulo especificado con su suscripción y plan activo.
     */
    @GetMapping("/validar/{codigoModulo}")
    public ResponseEntity<VerificarModuloRespuesta> validarAccesoModulo(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String codigoModulo) {
        ResultadoValidacionModulo resultado = validadorPermisosBackend.validarAccesoModuloPorToken(authHeader, codigoModulo);

        VerificarModuloRespuesta respuesta = VerificarModuloRespuesta.builder()
                .accesoPermitido(resultado.isAccesoPermitido())
                .codigoModulo(resultado.getCodigoModulo())
                .codigoPlan(resultado.getCodigoPlan())
                .estadoSuscripcion(resultado.getEstadoSuscripcion())
                .mensaje("Acceso autorizado al módulo " + resultado.getCodigoModulo())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Retorna la matriz completa de módulos y su estado de habilitación para el plan del cliente.
     */
    @GetMapping("/mis-modulos")
    public ResponseEntity<MatrizPermisosRespuesta> consultarMisModulos(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ExcepcionModuloRestringidoPorPlan("AUTENTICACION_REQUERIDA", "Token de acceso Bearer requerido");
        }

        String token = authHeader.substring(7).trim();
        ClaimsTokenInstitucional claims = proveedorTokenJwtInstitucional.validarYExtraerClaims(token);
        UUID idInstitucion = claims.getIdUsuario();

        SuscripcionInstitucional suscripcion = repositorioSuscripcionInstitucional
                .findFirstByIdInstitucionOrderByFechaExpiracionDesc(idInstitucion)
                .orElseThrow(() -> new ExcepcionModuloRestringidoPorPlan("SUSCRIPCION_NO_ENCONTRADA", "No se encontró suscripción"));

        boolean vigente = suscripcion.estaVigente();
        if (!vigente && "ACTIVA".equalsIgnoreCase(suscripcion.getEstadoSuscripcion())) {
            suscripcion.revocarPorExpiracion();
            repositorioSuscripcionInstitucional.save(suscripcion);
            validadorPermisosBackend.auditarCambioEstado(suscripcion.getIdSuscripcion(), "EXPIRADA");
        }

        PlanSuscripcion plan = repositorioPlanSuscripcion.findById(suscripcion.getIdPlan())
                .orElse(null);

        List<ModuloSistema> todosModulos = repositorioModuloSistema.findAll();
        List<MatrizPermisoModulo> matriz = plan != null
                ? repositorioMatrizPermisoModulo.findByIdPlan(plan.getIdProducto())
                : List.of();

        Map<UUID, MatrizPermisoModulo> mapaMatriz = matriz.stream()
                .collect(Collectors.toMap(MatrizPermisoModulo::getIdModulo, m -> m, (a, b) -> a));

        List<ModuloCatalogoDto> modulosDto = new ArrayList<>();
        for (ModuloSistema mod : todosModulos) {
            MatrizPermisoModulo p = mapaMatriz.get(mod.getIdModulo());
            boolean habilitado = vigente && p != null && p.isModuloHabilitado();
            String endpoint = p != null ? p.getEndpointRestringido() : "/api/v1/institutional/dashboard";

            modulosDto.add(ModuloCatalogoDto.builder()
                    .idModulo(mod.getIdModulo())
                    .codigoModulo(mod.getCodigoModulo())
                    .nombreModulo(mod.getNombreModulo())
                    .descripcion(mod.getDescripcion())
                    .esPremium(mod.isEsPremium())
                    .habilitadoEnPlan(habilitado)
                    .endpointRestringido(endpoint)
                    .build());
        }

        MatrizPermisosRespuesta respuesta = MatrizPermisosRespuesta.builder()
                .idInstitucion(idInstitucion)
                .idSuscripcion(suscripcion.getIdSuscripcion())
                .codigoPlan(plan != null ? plan.getCodigo() : "DESCONOCIDO")
                .nombrePlan(plan != null ? plan.getNombre() : "Suscripción Almara")
                .estadoSuscripcion(suscripcion.getEstadoSuscripcion())
                .fechaExpiracion(suscripcion.getFechaExpiracion())
                .suscripcionVigente(vigente)
                .modulos(modulosDto)
                .build();

        return ResponseEntity.ok(respuesta);
    }
}

package com.almara.modules.institutional.services;

import com.almara.modules.institutional.dto.CatalogoPlanesRespuesta;
import com.almara.modules.institutional.dto.ModuloCatalogoDto;
import com.almara.modules.institutional.dto.PlanSuscripcionCatalogoDto;
import com.almara.modules.institutional.models.MatrizPermisoModulo;
import com.almara.modules.institutional.models.ModuloSistema;
import com.almara.modules.institutional.models.PlanSuscripcion;
import com.almara.modules.institutional.repositories.RepositorioMatrizPermisoModulo;
import com.almara.modules.institutional.repositories.RepositorioModuloSistema;
import com.almara.modules.institutional.repositories.RepositorioPlanSuscripcion;
import com.almara.modules.institutional.security.ClaimsTokenInstitucional;
import com.almara.modules.institutional.security.ProveedorTokenJwtInstitucional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio de Aplicación para la consulta del Catálogo de Planes SaaS (HU-19).
 */
@Service
public class ServicioCatalogoPlanesSaaS {

    private static final Logger log = LoggerFactory.getLogger(ServicioCatalogoPlanesSaaS.class);

    private final RepositorioPlanSuscripcion repositorioPlanSuscripcion;
    private final RepositorioModuloSistema repositorioModuloSistema;
    private final RepositorioMatrizPermisoModulo repositorioMatrizPermisoModulo;
    private final ProveedorTokenJwtInstitucional proveedorTokenJwtInstitucional;

    public ServicioCatalogoPlanesSaaS(
            RepositorioPlanSuscripcion repositorioPlanSuscripcion,
            RepositorioModuloSistema repositorioModuloSistema,
            RepositorioMatrizPermisoModulo repositorioMatrizPermisoModulo,
            ProveedorTokenJwtInstitucional proveedorTokenJwtInstitucional) {
        this.repositorioPlanSuscripcion = repositorioPlanSuscripcion;
        this.repositorioModuloSistema = repositorioModuloSistema;
        this.repositorioMatrizPermisoModulo = repositorioMatrizPermisoModulo;
        this.proveedorTokenJwtInstitucional = proveedorTokenJwtInstitucional;
    }

    /**
     * Consulta el catálogo de planes SaaS activos con sus módulos habilitados/restringidos.
     * Si se envía un token válido, identifica el plan actual del cliente.
     */
    @Transactional(readOnly = true)
    public CatalogoPlanesRespuesta consultarCatalogoPlanes(String encabezadoAuthorization) {
        long inicioMs = System.currentTimeMillis();

        String codigoPlanActualCliente = null;
        if (encabezadoAuthorization != null && encabezadoAuthorization.startsWith("Bearer ")) {
            String token = encabezadoAuthorization.substring(7).trim();
            try {
                ClaimsTokenInstitucional claims = proveedorTokenJwtInstitucional.validarYExtraerClaims(token);
                codigoPlanActualCliente = claims.getCodigoPlan();
                log.debug("Cliente autenticado en catálogo con plan actual: {}", codigoPlanActualCliente);
            } catch (Exception ex) {
                log.debug("Token de autorización no válido o revocado en consulta de catálogo: {}", ex.getMessage());
            }
        }

        List<ModuloSistema> todosLosModulos = repositorioModuloSistema.findAll();
        List<PlanSuscripcion> planesActivos = repositorioPlanSuscripcion.findByActivoTrueOrderByPrecioBaseAsc();

        List<PlanSuscripcionCatalogoDto> planesDto = new ArrayList<>();

        for (PlanSuscripcion plan : planesActivos) {
            List<MatrizPermisoModulo> permisosPlan = repositorioMatrizPermisoModulo.findByIdPlan(plan.getIdProducto());
            Map<java.util.UUID, MatrizPermisoModulo> mapaPermisos = permisosPlan.stream()
                    .collect(Collectors.toMap(MatrizPermisoModulo::getIdModulo, Function.identity(), (a, b) -> a));

            List<ModuloCatalogoDto> modulosDto = new ArrayList<>();
            for (ModuloSistema modulo : todosLosModulos) {
                MatrizPermisoModulo permiso = mapaPermisos.get(modulo.getIdModulo());
                boolean habilitado = permiso != null && permiso.isModuloHabilitado();
                String endpointRestringido = permiso != null ? permiso.getEndpointRestringido() : "";

                modulosDto.add(ModuloCatalogoDto.builder()
                        .idModulo(modulo.getIdModulo())
                        .codigoModulo(modulo.getCodigoModulo())
                        .nombreModulo(modulo.getNombreModulo())
                        .descripcion(modulo.getDescripcion())
                        .esPremium(modulo.isEsPremium())
                        .habilitadoEnPlan(habilitado)
                        .endpointRestringido(endpointRestringido)
                        .build());
            }

            boolean esPlanActual = codigoPlanActualCliente != null
                    && codigoPlanActualCliente.equalsIgnoreCase(plan.getCodigo());

            planesDto.add(PlanSuscripcionCatalogoDto.builder()
                    .idProducto(plan.getIdProducto())
                    .codigo(plan.getCodigo())
                    .nombre(plan.getNombre())
                    .descripcion(plan.getDescripcion())
                    .precioBase(plan.getPrecioBase())
                    .precioFinal(plan.calcularPrecioFinal())
                    .tipoPlan(plan.getTipoPlan())
                    .duracionDias(plan.getDuracionDias())
                    .esGratuito(plan.isEsGratuito())
                    .esPlanActual(esPlanActual)
                    .modulos(modulosDto)
                    .build());
        }

        long tiempoConsultaMs = System.currentTimeMillis() - inicioMs;
        log.info("Catálogo de planes consultado: totalPlanes={}, tiempoMs={}", planesDto.size(), tiempoConsultaMs);

        return CatalogoPlanesRespuesta.builder()
                .planes(planesDto)
                .totalPlanesActivos(planesDto.size())
                .codigoPlanActualCliente(codigoPlanActualCliente)
                .tiempoConsultaMs(tiempoConsultaMs)
                .build();
    }
}

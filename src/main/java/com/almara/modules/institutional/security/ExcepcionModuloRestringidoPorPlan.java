package com.almara.modules.institutional.security;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Excepción lanzada cuando un cliente institucional intenta acceder a un módulo restringido o su suscripción expiró (HU-20).
 */
public class ExcepcionModuloRestringidoPorPlan extends ExcepcionReglaNegocioInstitucional {

    public ExcepcionModuloRestringidoPorPlan(String codigoError, String mensaje) {
        super(HttpStatus.FORBIDDEN, codigoError, mensaje);
    }

    public ExcepcionModuloRestringidoPorPlan(String codigoError, String mensaje, Map<String, Object> detallesAdicionales) {
        super(HttpStatus.FORBIDDEN, codigoError, mensaje, detallesAdicionales);
    }
}

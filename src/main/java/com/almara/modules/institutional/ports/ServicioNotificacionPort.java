package com.almara.modules.institutional.ports;

/**
 * Puerto de dominio para el despacho de notificaciones transaccionales y correos (SMTP/Email).
 */
public interface ServicioNotificacionPort {

    void enviarCorreoVerificacion(String destinatario, String razonSocial, String tokenVerificacion);

    void enviarCorreoConfirmacion(String destinatario, String asunto, String cuerpoMensaje);
}

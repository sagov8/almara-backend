package com.almara.modules.institutional.adapters.notification;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de rendimiento, propiedades SMTP y resiliencia para AdaptadorNotificacionSmtpReal.
 */
class AdaptadorNotificacionSmtpRealPrueba {

    @Test
    @DisplayName("Debe configurar propiedades de optimización SMTP (localhost, quitwait=false, TLS) y refrescar Session en JavaMailSenderImpl")
    void debeConfigurarPropiedadesOptimizacionSmtpEnJavaMailSenderImpl() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        // Inicializar Session previamente para comprobar que AdaptadorNotificacionSmtpReal invalida la caché de Session
        Session sesionPrevia = mailSender.getSession();
        assertNull(sesionPrevia.getProperty("mail.smtp.localhost"));

        AdaptadorNotificacionSmtpReal adaptador = new AdaptadorNotificacionSmtpReal(mailSender, "no-reply@almara.co");

        Properties props = mailSender.getJavaMailProperties();
        assertEquals("localhost", props.getProperty("mail.smtp.localhost"),
                "Debe fijar mail.smtp.localhost=localhost para evitar bloqueo de 5-15s por reverse DNS en EHLO");
        assertEquals("false", props.getProperty("mail.smtp.quitwait"),
                "Debe fijar mail.smtp.quitwait=false para no bloquear esperando respuesta 221 de QUIT");
        assertEquals("*", props.getProperty("mail.smtp.ssl.trust"));
        assertEquals("TLSv1.2 TLSv1.3", props.getProperty("mail.smtp.ssl.protocols"));
        assertEquals("3000", props.getProperty("mail.smtp.connectiontimeout"));
        assertEquals("3000", props.getProperty("mail.smtp.timeout"));
        assertEquals("3000", props.getProperty("mail.smtp.writetimeout"));

        Session sesionOptimizada = mailSender.getSession();
        assertEquals("localhost", sesionOptimizada.getProperty("mail.smtp.localhost"),
                "La Session activa de Jakarta Mail debe reflejar las propiedades optimizadas");
        assertEquals("no-reply@almara.co", sesionOptimizada.getProperty("mail.from"),
                "Debe fijar mail.from en Session para evitar que MimeMessage.saveChanges() ejecute InetAddress.getLocalHost() (~9.8s en Windows)");
        assertEquals("localhost", sesionOptimizada.getProperty("mail.host"));
        assertEquals("false", sesionOptimizada.getProperty("mail.smtp.quitwait"));

        // Verificar que createMimeMessage() + saveChanges() (invocado por JavaMailSenderImpl.doSend) toma < 200ms y no ~9.8s
        long inicioSaveChanges = System.currentTimeMillis();
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        assertDoesNotThrow(() -> mimeMessage.saveChanges());
        long duracionSaveChanges = System.currentTimeMillis() - inicioSaveChanges;
        assertTrue(duracionSaveChanges < 300L,
                "MimeMessage.saveChanges() no debe bloquearse en DNS local (tardó " + duracionSaveChanges + " ms)");

        adaptador.cerrarExecutor();
    }

    @Test
    @DisplayName("Debe despachar correo de verificación y confirmación rápidamente cuando SMTP responde en tiempo normal")
    void debeDespacharCorreoVerificacionYConfirmacionExitosamente() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenAnswer(inv -> new MimeMessage((Session) null));

        AdaptadorNotificacionSmtpReal adaptador = new AdaptadorNotificacionSmtpReal(
                mailSender, "no-reply@almara.co", 2500L, 1200L
        );

        adaptador.enviarCorreoVerificacion("contacto@popayan.gov.co", "Alcaldía de Popayán", "654321");
        adaptador.enviarCorreoConfirmacion("contacto@popayan.gov.co", "Pago confirmado", "Detalle del pago");

        verify(mailSender, times(2)).send(any(MimeMessage.class));
        adaptador.cerrarExecutor();
    }

    @Test
    @DisplayName("Debe liberar la petición en ventana acotada (<3s) y finalizar el envío en segundo plano si el servidor SMTP tarda demasiado")
    void debeLiberarRespuestaEnVentanaAcotadaSiSmtpSeDemora() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenAnswer(inv -> new MimeMessage((Session) null));

        CountDownLatch envioCompletadoEnFondo = new CountDownLatch(1);
        doAnswer(inv -> {
            Thread.sleep(900L);
            envioCompletadoEnFondo.countDown();
            return null;
        }).when(mailSender).send(any(MimeMessage.class));

        // Configuramos espera máxima de 250ms para verificar que libera antes de los 900ms del servidor SMTP lento
        AdaptadorNotificacionSmtpReal adaptador = new AdaptadorNotificacionSmtpReal(
                mailSender, "no-reply@almara.co", 250L, 200L
        );

        long inicio = System.currentTimeMillis();
        adaptador.enviarCorreoVerificacion("lento@popayan.gov.co", "Entidad Prueba", "123456");
        long duracionRespuestaHttp = System.currentTimeMillis() - inicio;

        assertTrue(duracionRespuestaHttp < 700L,
                "La llamada debe retornar en tiempo acotado sin bloquearse los 900ms completos (tardó " + duracionRespuestaHttp + " ms)");
        assertTrue(envioCompletadoEnFondo.await(2, TimeUnit.SECONDS),
                "El hilo en segundo plano debe completar el envío SMTP sin ser cancelado");

        adaptador.cerrarExecutor();
    }

    @Test
    @DisplayName("Debe propagar ExcepcionReglaNegocioInstitucional (502 BAD_GATEWAY) si el servidor SMTP falla de inmediato en verificación")
    void debePropagarExcepcionSiSmtpFallaInmediatamenteEnVerificacion() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenAnswer(inv -> new MimeMessage((Session) null));
        doThrow(new MailSendException("Credenciales SMTP rechazadas")).when(mailSender).send(any(MimeMessage.class));

        AdaptadorNotificacionSmtpReal adaptador = new AdaptadorNotificacionSmtpReal(
                mailSender, "no-reply@almara.co", 2500L, 1200L
        );

        ExcepcionReglaNegocioInstitucional ex = assertThrows(
                ExcepcionReglaNegocioInstitucional.class,
                () -> adaptador.enviarCorreoVerificacion("error@popayan.gov.co", "Entidad Error", "999999")
        );

        assertEquals(HttpStatus.BAD_GATEWAY, ex.getEstado());
        assertEquals("ERROR_ENVIO_CORREO_SMTP", ex.getCodigoError());
        adaptador.cerrarExecutor();
    }
}

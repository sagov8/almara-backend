package com.almara.modules.institutional.adapters.notification;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.institutional.ports.ServicioNotificacionPort;
import jakarta.annotation.PreDestroy;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adaptador de producción optimizado para el envío de correos y notificaciones transaccionales
 * utilizando JavaMailSender sobre protocolo SMTP (Gmail, SendGrid, Amazon SES, Mailgun, etc.).
 * Se activa configurando almara.notificaciones.proveedor=smtp.
 *
 * Incluye optimizaciones de transporte SMTP (eliminación de bloqueo por reverse DNS en EHLO,
 * quitwait=false, preferencia IPv4, payload HTML plano sin sobrecarga multipart anidada y
 * despacho asíncrono con tiempo de espera acotado) para garantizar respuestas entre 2 y 5 segundos.
 */
@Component
@ConditionalOnProperty(name = "almara.notificaciones.proveedor", havingValue = "smtp")
public class AdaptadorNotificacionSmtpReal implements ServicioNotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(AdaptadorNotificacionSmtpReal.class);

    private static final long ESPERA_MAXIMA_VERIFICACION_DEFAULT_MS = 2200L;
    private static final long ESPERA_MAXIMA_CONFIRMACION_DEFAULT_MS = 500L;

    private final JavaMailSender mailSender;
    private final String remitente;
    private final long esperaMaximaVerificacionMs;
    private final long esperaMaximaConfirmacionMs;
    private final ExecutorService smtpExecutor;

    public AdaptadorNotificacionSmtpReal(
            JavaMailSender mailSender,
            String remitente) {
        this(mailSender, remitente, ESPERA_MAXIMA_VERIFICACION_DEFAULT_MS, ESPERA_MAXIMA_CONFIRMACION_DEFAULT_MS);
    }

    @Autowired
    public AdaptadorNotificacionSmtpReal(
            JavaMailSender mailSender,
            @Value("${almara.notificaciones.remitente:${spring.mail.username:no-reply@almara.co}}") String remitente,
            @Value("${almara.notificaciones.smtp.espera-maxima-verificacion-ms:2200}") long esperaMaximaVerificacionMs,
            @Value("${almara.notificaciones.smtp.espera-maxima-confirmacion-ms:500}") long esperaMaximaConfirmacionMs) {
        this.mailSender = mailSender;
        this.remitente = (remitente != null && !remitente.isBlank()) ? remitente : "no-reply@almara.co";
        this.esperaMaximaVerificacionMs = Math.max(100L, esperaMaximaVerificacionMs);
        this.esperaMaximaConfirmacionMs = Math.max(100L, esperaMaximaConfirmacionMs);

        AtomicInteger contadorHilos = new AtomicInteger(1);
        this.smtpExecutor = Executors.newCachedThreadPool(runnable -> {
            Thread hilo = new Thread(runnable, "almara-smtp-worker-" + contadorHilos.getAndIncrement());
            hilo.setDaemon(true);
            return hilo;
        });

        optimizarPropiedadesJavaMail(mailSender);
    }

    private void optimizarPropiedadesJavaMail(JavaMailSender sender) {
        // Evitar demoras de 5s en redes Windows dual-stack al resolver/conectar a smtp.gmail.com por IPv6
        if (System.getProperty("java.net.preferIPv4Stack") == null) {
            System.setProperty("java.net.preferIPv4Stack", "true");
        }

        if (sender instanceof JavaMailSenderImpl senderImpl) {
            if (senderImpl.getDefaultEncoding() == null) {
                senderImpl.setDefaultEncoding(StandardCharsets.UTF_8.name());
            }
            Properties props = new Properties();
            props.putAll(senderImpl.getJavaMailProperties());
            // Evita que MimeMessage.saveChanges() -> InternetAddress.getLocalAddress(session) ejecute InetAddress.getLocalHost() (bloqueo de ~9.8s en Windows)
            props.putIfAbsent("mail.from", this.remitente);
            props.putIfAbsent("mail.host", "localhost");
            // Evita que Jakarta Mail ejecute InetAddress.getLocalHost().getCanonicalHostName() (reverse DNS de 5-15s en Windows) en EHLO
            props.putIfAbsent("mail.smtp.localhost", "localhost");
            // Retorna inmediatamente tras recibir 250 OK de DATA sin esperar el cierre TCP del comando QUIT
            props.putIfAbsent("mail.smtp.quitwait", "false");
            // Acelera la negociación STARTTLS evitando bloqueos de validación OCSP/CRL lentos
            props.putIfAbsent("mail.smtp.ssl.trust", "*");
            props.putIfAbsent("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            props.putIfAbsent("mail.smtp.connectiontimeout", "3000");
            props.putIfAbsent("mail.smtp.timeout", "3000");
            props.putIfAbsent("mail.smtp.writetimeout", "3000");

            // Reasignar propiedades mediante setJavaMailProperties para invalidar cualquier Session en caché previa
            senderImpl.setJavaMailProperties(props);

            // Precalentar Session y contexto TLS en segundo plano para eliminar penalización cold-start
            smtpExecutor.submit(() -> {
                try {
                    senderImpl.getSession();
                    javax.net.ssl.SSLContext.getDefault();
                } catch (Exception ignored) {
                    // Ignorar en precalentamiento asíncrono
                }
            });
        }
    }

    @PreDestroy
    public void cerrarExecutor() {
        smtpExecutor.shutdown();
    }

    @Override
    public void enviarCorreoVerificacion(String destinatario, String razonSocial, String tokenVerificacion) {
        long inicioMs = System.currentTimeMillis();
        log.info("[SMTP REAL] Iniciando envío de correo de verificación a {} ({})", destinatario, razonSocial);

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            // multipart=false genera un cuerpo text/html directo sin sobrecarga de contenedores MIME anidados
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, StandardCharsets.UTF_8.name());

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("Almara — Código de Verificación para tu Cuenta Institucional: " + razonSocial);

            String contenidoHtml = construirPlantillaHtmlVerificacion(razonSocial, tokenVerificacion);
            helper.setText(contenidoHtml, true);

            despacharConEsperaAcotada(mensaje, destinatario, "verificación", esperaMaximaVerificacionMs, true, inicioMs);

        } catch (MessagingException | MailException e) {
            log.error("[SMTP REAL] Error al despachar correo SMTP a {}: {}", destinatario, e.getMessage(), e);
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_GATEWAY,
                    "ERROR_ENVIO_CORREO_SMTP",
                    "No se pudo enviar el correo de verificación vía SMTP (" + e.getMessage() + "). Verifique las credenciales de correo en application.yml."
            );
        }
    }

    @Override
    public void enviarCorreoConfirmacion(String destinatario, String asunto, String cuerpoMensaje) {
        long inicioMs = System.currentTimeMillis();
        log.info("[SMTP REAL] Iniciando envío de correo de confirmación a {} con asunto '{}'", destinatario, asunto);

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, StandardCharsets.UTF_8.name());

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);

            String contenidoHtml = construirPlantillaHtmlConfirmacion(asunto, cuerpoMensaje);
            helper.setText(contenidoHtml, true);

            despacharConEsperaAcotada(mensaje, destinatario, "confirmación", esperaMaximaConfirmacionMs, false, inicioMs);

        } catch (Exception e) {
            log.warn("[SMTP REAL] No fue posible enviar la confirmación secundaria a {}: {}", destinatario, e.getMessage());
        }
    }

    private void despacharConEsperaAcotada(
            MimeMessage mensaje,
            String destinatario,
            String tipoCorreo,
            long esperaMaximaMs,
            boolean propagarErrorCritico,
            long inicioMs) {

        CompletableFuture<Void> envioFuture = CompletableFuture.runAsync(() -> {
            mailSender.send(mensaje);
            long duracionTotal = System.currentTimeMillis() - inicioMs;
            log.info("[SMTP REAL] Correo de {} enviado con éxito a {} en {} ms", tipoCorreo, destinatario, duracionTotal);
        }, smtpExecutor);

        envioFuture.exceptionally(ex -> {
            Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
            log.warn("[SMTP REAL] Fallo en envío de correo de {} a {}: {}", tipoCorreo, destinatario, causa.getMessage());
            return null;
        });

        try {
            envioFuture.get(esperaMaximaMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException timeoutEx) {
            // No cancelamos envioFuture: el hilo worker termina el handshake SMTP en segundo plano
            // mientras liberamos la respuesta HTTP dentro de la ventana objetivo de 2-5 segundos.
            long transcurrido = System.currentTimeMillis() - inicioMs;
            log.info("[SMTP REAL] El envío de correo de {} a {} supera {} ms ({} ms transcurridos); "
                    + "continúa procesándose en segundo plano para responder al usuario sin demora.",
                    tipoCorreo, destinatario, esperaMaximaMs, transcurrido);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("[SMTP REAL] Espera interrumpida durante envío de correo de {} a {}", tipoCorreo, destinatario);
        } catch (ExecutionException ee) {
            Throwable causa = ee.getCause() != null ? ee.getCause() : ee;
            if (propagarErrorCritico) {
                if (causa instanceof MailException mailEx) {
                    throw mailEx;
                }
                throw new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.BAD_GATEWAY,
                        "ERROR_ENVIO_CORREO_SMTP",
                        "No se pudo enviar el correo de verificación vía SMTP (" + causa.getMessage() + "). Verifique las credenciales de correo en application.yml."
                );
            } else {
                log.warn("[SMTP REAL] No fue posible completar el correo de {} a {}: {}", tipoCorreo, destinatario, causa.getMessage());
            }
        }
    }

    private String construirPlantillaHtmlVerificacion(String razonSocial, String tokenVerificacion) {
        String plantilla = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; color: #1e293b; margin: 0; padding: 20px; }
                .container { max-width: 580px; margin: 0 auto; background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
                .header { background: linear-gradient(135deg, #0284c7 0%, #4f46e5 100%); padding: 32px 24px; text-align: center; color: #ffffff; }
                .header h1 { margin: 0; font-size: 24px; font-weight: 800; letter-spacing: -0.5px; }
                .header p { margin: 6px 0 0 0; font-size: 13px; opacity: 0.9; }
                .content { padding: 32px 28px; line-height: 1.6; font-size: 14px; }
                .code-box { background-color: #f0f9ff; border: 2px dashed #0284c7; border-radius: 12px; padding: 20px; text-align: center; margin: 28px 0; }
                .code-title { font-size: 11px; text-transform: uppercase; font-weight: 700; color: #0369a1; letter-spacing: 1px; margin-bottom: 8px; }
                .code-value { font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #0c4a6e; font-family: 'Courier New', Courier, monospace; }
                .footer { background-color: #f8fafc; padding: 20px 24px; border-top: 1px solid #e2e8f0; font-size: 12px; color: #64748b; text-align: center; }
                .badge { display: inline-block; background-color: #e0f2fe; color: #0369a1; padding: 4px 10px; border-radius: 9999px; font-weight: 600; font-size: 12px; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>ALMARA INSTITUCIONAL</h1>
                  <p>Plataforma de Inteligencia Urbana y Bienestar Ciudadano</p>
                </div>
                <div class="content">
                  <p>Estimado(a) representante de <strong>{{RAZON_SOCIAL}}</strong>,</p>
                  <p>Hemos recibido la solicitud de registro corporativo para su entidad en la plataforma Almara. Para verificar su dirección de correo electrónico institucional y activar su cuenta corporativa, ingrese el siguiente código de validación:</p>
                  
                  <div class="code-box">
                    <div class="code-title">Código de Validación Institucional</div>
                    <div class="code-value">{{TOKEN_VERIFICACION}}</div>
                  </div>
                  
                  <p style="font-size: 13px; color: #475569;">
                    🔒 <strong>Seguridad:</strong> Este código es de uso único y confidencial. Si usted o su institución no han solicitado este registro, por favor ignore este mensaje.
                  </p>
                </div>
                <div class="footer">
                  <p style="margin: 0;">© 2026 Almara Urban Intelligence Platform. Popayán, Colombia.</p>
                </div>
              </div>
            </body>
            </html>
            """;
        return plantilla
                .replace("{{RAZON_SOCIAL}}", razonSocial != null ? razonSocial : "")
                .replace("{{TOKEN_VERIFICACION}}", tokenVerificacion != null ? tokenVerificacion : "");
    }

    private String construirPlantillaHtmlConfirmacion(String asunto, String cuerpoMensaje) {
        String plantilla = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; color: #1e293b; margin: 0; padding: 20px; }
                .container { max-width: 580px; margin: 0 auto; background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
                .header { background: linear-gradient(135deg, #10b981 0%, #059669 100%); padding: 32px 24px; text-align: center; color: #ffffff; }
                .header h1 { margin: 0; font-size: 24px; font-weight: 800; }
                .content { padding: 32px 28px; line-height: 1.6; font-size: 14px; white-space: pre-line; }
                .footer { background-color: #f8fafc; padding: 20px 24px; border-top: 1px solid #e2e8f0; font-size: 12px; color: #64748b; text-align: center; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>ALMARA INSTITUCIONAL</h1>
                  <p>{{ASUNTO}}</p>
                </div>
                <div class="content">
                  {{CUERPO}}
                </div>
                <div class="footer">
                  <p style="margin: 0;">© 2026 Almara Urban Intelligence Platform.</p>
                </div>
              </div>
            </body>
            </html>
            """;
        return plantilla
                .replace("{{ASUNTO}}", asunto != null ? asunto : "")
                .replace("{{CUERPO}}", cuerpoMensaje != null ? cuerpoMensaje : "");
    }
}

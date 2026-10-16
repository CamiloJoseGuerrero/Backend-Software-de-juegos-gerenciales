package com.estratego.infrastructure.mail;

import com.estratego.application.event.EstudiantesCargadosEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Locale;

/**
 * Envía a cada estudiante recién cargado su usuario y contraseña inicial.
 * Corre después del commit y en segundo plano: un correo que falla no afecta la carga.
 * No envía a dominios reservados para pruebas (.local, .test, .example, .invalid, .localhost, example.com…):
 * esos correos rebotan y dañan la reputación de la cuenta que envía.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CredencialesEmailListener {

    private final ObjectProvider<JavaMailSender> mailSender;

    /** TLD reservados para pruebas (RFC 2606 / 6761) y dominios de ejemplo. */
    private static final List<String> TLD_DE_PRUEBA = List.of(".local", ".test", ".example", ".invalid", ".localhost");
    private static final List<String> DOMINIOS_DE_PRUEBA = List.of("example.com", "example.net", "example.org", "localhost");

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    @Value("${app.mail.from:}")
    private String from;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void enviarCredenciales(EstudiantesCargadosEvent event) {
        if (!enabled) {
            log.info("Envío de correos desactivado (MAIL_ENABLED=false): {} credenciales sin enviar",
                    event.credenciales().size());
            return;
        }

        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null || from == null || from.isBlank()) {
            log.warn("Correo activado pero sin SMTP configurado (MAIL_USERNAME / MAIL_FROM)");
            return;
        }

        int enviados = 0;
        int omitidos = 0;
        for (EstudiantesCargadosEvent.Credencial c : event.credenciales()) {
            if (esDominioDePrueba(c.correo())) {
                omitidos++;
                log.info("Correo no enviado a {}: dominio de prueba", c.correo());
                continue;
            }
            try {
                sender.send(construirMensaje(c));
                enviados++;
            } catch (MailException ex) {
                log.warn("No se pudo enviar el correo a {}: {}", c.correo(), ex.getMessage());
            }
        }
        log.info("Correos de credenciales enviados: {} de {}{}", enviados, event.credenciales().size(),
                omitidos > 0 ? " (" + omitidos + " omitidos por dominio de prueba)" : "");
    }

    /** true si el dominio del correo es de prueba o no tiene dominio válido. */
    static boolean esDominioDePrueba(String correo) {
        if (correo == null) return true;
        int arroba = correo.lastIndexOf('@');
        if (arroba < 0 || arroba == correo.length() - 1) return true;
        String dominio = correo.substring(arroba + 1).trim().toLowerCase(Locale.ROOT);
        if (dominio.endsWith(".")) dominio = dominio.substring(0, dominio.length() - 1);
        for (String tld : TLD_DE_PRUEBA) {
            if (dominio.endsWith(tld) || dominio.equals(tld.substring(1))) return true;
        }
        for (String d : DOMINIOS_DE_PRUEBA) {
            if (dominio.equals(d) || dominio.endsWith("." + d)) return true;
        }
        return false;
    }

    SimpleMailMessage construirMensaje(EstudiantesCargadosEvent.Credencial c) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(c.correo());
        msg.setSubject("Tu acceso a Estratego");
        msg.setText("""
                Hola %s,

                Tu docente te registró en Estratego, el simulador de juegos gerenciales.

                Para ingresar usa:
                  Correo:     %s
                  Usuario:    %s
                  Contraseña: %s

                Ingresa en: %s

                Te recomendamos cambiar la contraseña después de tu primer ingreso.
                Si no esperabas este correo, puedes ignorarlo.
                """.formatted(c.nombre(), c.correo(), c.usuario(), c.contrasena(), frontendUrl));
        return msg;
    }
}

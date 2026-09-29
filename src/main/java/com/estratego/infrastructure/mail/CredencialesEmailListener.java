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

/**
 * Envía a cada estudiante recién cargado su usuario y contraseña inicial.
 * Corre después del commit y en segundo plano: un correo que falla no afecta la carga.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CredencialesEmailListener {

    private final ObjectProvider<JavaMailSender> mailSender;

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
        for (EstudiantesCargadosEvent.Credencial c : event.credenciales()) {
            try {
                sender.send(construirMensaje(c));
                enviados++;
            } catch (MailException ex) {
                log.warn("No se pudo enviar el correo a {}: {}", c.correo(), ex.getMessage());
            }
        }
        log.info("Correos de credenciales enviados: {} de {}", enviados, event.credenciales().size());
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

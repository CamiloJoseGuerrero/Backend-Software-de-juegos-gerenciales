package com.estratego.infrastructure.mail;

import com.estratego.application.event.EstudiantesCargadosEvent;
import com.estratego.application.event.EstudiantesCargadosEvent.Credencial;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredencialesEmailListenerTest {

    @Mock private ObjectProvider<JavaMailSender> provider;
    @Mock private JavaMailSender sender;

    private final EstudiantesCargadosEvent evento = new EstudiantesCargadosEvent(List.of(
            new Credencial("Ana", "ana@test.com", "ana", "Usu-001-123!"),
            new Credencial("Luis", "luis@test.com", "luis", "Usu-001-456!")));

    private CredencialesEmailListener listener(boolean enabled) {
        CredencialesEmailListener l = new CredencialesEmailListener(provider);
        ReflectionTestUtils.setField(l, "enabled", enabled);
        ReflectionTestUtils.setField(l, "from", "estratego@test.com");
        ReflectionTestUtils.setField(l, "frontendUrl", "http://front");
        return l;
    }

    @Test
    void desactivadoNoEnviaNada() {
        listener(false).enviarCredenciales(evento);
        verifyNoInteractions(provider);
    }

    @Test
    void enviaUnCorreoPorEstudianteConSusCredenciales() {
        when(provider.getIfAvailable()).thenReturn(sender);

        listener(true).enviarCredenciales(evento);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender, times(2)).send(captor.capture());
        SimpleMailMessage primero = captor.getAllValues().get(0);
        assertArrayEquals(new String[]{"ana@test.com"}, primero.getTo());
        assertTrue(primero.getText().contains("Usu-001-123!"));
        assertTrue(primero.getText().contains("http://front"));
    }

    @Test
    void unCorreoFallidoNoDetieneLosDemas() {
        when(provider.getIfAvailable()).thenReturn(sender);
        doThrow(new MailSendException("SMTP caído")).doNothing().when(sender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> listener(true).enviarCredenciales(evento));
        verify(sender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void noEnviaADominiosDePrueba() {
        when(provider.getIfAvailable()).thenReturn(sender);
        EstudiantesCargadosEvent mezcla = new EstudiantesCargadosEvent(List.of(
                new Credencial("Prueba", "x@estratego-test.local", "x", "Usu-001-1!"),
                new Credencial("Ejemplo", "y@example.com", "y", "Usu-001-2!"),
                new Credencial("Real", "real@unicolombo.edu.co", "real", "Usu-001-3!")));

        listener(true).enviarCredenciales(mezcla);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender, times(1)).send(captor.capture());
        assertArrayEquals(new String[]{"real@unicolombo.edu.co"}, captor.getValue().getTo());
    }

    @Test
    void reconoceLosDominiosDePrueba() {
        for (String c : List.of("a@b.local", "a@algo.test", "a@x.example", "a@x.invalid", "a@localhost",
                "a@example.com", "a@sub.example.org", "sin-arroba", "a@")) {
            assertTrue(CredencialesEmailListener.esDominioDePrueba(c), c);
        }
        for (String c : List.of("ana@test.com", "a@gmail.com", "a@unicolombo.edu.co", "a@correo.edu.co", "a@outlook.com")) {
            assertFalse(CredencialesEmailListener.esDominioDePrueba(c), c);
        }
    }
}

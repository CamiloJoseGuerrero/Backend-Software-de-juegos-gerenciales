package com.estratego.application.usecase;

import com.estratego.application.dto.docente.CargaMasivaResponse;
import com.estratego.domain.model.usuario.Estudiante;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.EstudianteRepository;
import com.estratego.domain.repository.UsuarioRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargaMasivaEstudiantesServiceTest {

    private static final String CORREO_DOCENTE = "docente@correo.com";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CargaMasivaEstudiantesService service;

    @Test
    void procesaArchivoConOrdenCorreoPrimero() throws IOException {
        MockMultipartFile archivo = crearArchivo(
                new String[]{"correo", "nombre", "numeroIdentificacion", "edad", "genero"},
                new String[]{"ana@correo.com", "Ana Perez", "123456", "20", "F"}
        );

        when(usuarioRepository.existsByCorreo("ana@correo.com")).thenReturn(false);
        when(usuarioRepository.existsByNumeroIdentificacion("123456")).thenReturn(false);
        when(usuarioRepository.existsByUsuario("ana")).thenReturn(false);
        when(passwordEncoder.encode("Usu-001-123456!")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(inv -> inv.getArgument(0));

        CargaMasivaResponse resultado = service.procesar(archivo, CORREO_DOCENTE);

        assertEquals(1, resultado.getCreados().size());
        assertTrue(resultado.getErrores().isEmpty());

        var creado = resultado.getCreados().get(0);
        assertEquals("ana@correo.com", creado.getCorreo());
        assertEquals("Ana Perez", creado.getNombre());
        assertEquals(20, creado.getEdad());
        assertEquals("F", creado.getGenero());
        assertEquals("Usu-001-123456!", creado.getContrasenaGenerada());

        ArgumentCaptor<Estudiante> captorEstudiante = ArgumentCaptor.forClass(Estudiante.class);
        verify(estudianteRepository).save(captorEstudiante.capture());
        assertEquals(20, captorEstudiante.getValue().getEdad());
        assertEquals("F", captorEstudiante.getValue().getGenero());
    }

    @Test
    void procesaArchivoConOrdenNombrePrimero() throws IOException {
        MockMultipartFile archivo = crearArchivo(
                new String[]{"nombre", "correo", "numeroIdentificacion"},
                new String[]{"Ana Perez", "ana@correo.com", "123456"}
        );

        when(usuarioRepository.existsByCorreo("ana@correo.com")).thenReturn(false);
        when(usuarioRepository.existsByNumeroIdentificacion("123456")).thenReturn(false);
        when(usuarioRepository.existsByUsuario("ana")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(inv -> inv.getArgument(0));

        CargaMasivaResponse resultado = service.procesar(archivo, CORREO_DOCENTE);

        assertEquals(1, resultado.getCreados().size());
        assertEquals("ana@correo.com", resultado.getCreados().get(0).getCorreo());
        assertEquals("Ana Perez", resultado.getCreados().get(0).getNombre());
    }

    @Test
    void rechazaEdadInvalida() throws IOException {
        MockMultipartFile archivo = crearArchivo(
                new String[]{"correo", "nombre", "numeroIdentificacion", "edad", "genero"},
                new String[]{"ana@correo.com", "Ana Perez", "123456", "abc", "F"}
        );

        CargaMasivaResponse resultado = service.procesar(archivo, CORREO_DOCENTE);

        assertEquals(1, resultado.getErrores().size());
        assertEquals("La edad debe ser un número entero", resultado.getErrores().get(0).getMensaje());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void rechazaGeneroInvalido() throws IOException {
        MockMultipartFile archivo = crearArchivo(
                new String[]{"correo", "nombre", "numeroIdentificacion", "edad", "genero"},
                new String[]{"ana@correo.com", "Ana Perez", "123456", "20", "X"}
        );

        CargaMasivaResponse resultado = service.procesar(archivo, CORREO_DOCENTE);

        assertEquals(1, resultado.getErrores().size());
        assertEquals("El género debe ser M o F", resultado.getErrores().get(0).getMensaje());
    }

    @Test
    void saltaFilasCompletamenteVacias() throws IOException {
        MockMultipartFile archivo = crearArchivo(
                new String[]{"correo", "nombre", "numeroIdentificacion", "edad", "genero"},
                new String[]{"", "", "", "", ""}
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.procesar(archivo, CORREO_DOCENTE));
    }

    @Test
    void informaCorreoDuplicadoSinCrearUsuario() throws IOException {
        MockMultipartFile archivo = crearArchivo(
                new String[]{"correo", "nombre", "numeroIdentificacion", "edad", "genero"},
                new String[]{"ana@correo.com", "Ana Perez", "123456", "20", "F"}
        );
        when(usuarioRepository.existsByCorreo("ana@correo.com")).thenReturn(true);

        CargaMasivaResponse resultado = service.procesar(archivo, CORREO_DOCENTE);

        assertEquals(1, resultado.getErrores().size());
        assertEquals("El correo ya está registrado", resultado.getErrores().get(0).getMensaje());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    private MockMultipartFile crearArchivo(String[] headers, String[] datos) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = wb.createSheet("Estudiantes");
            var encabezado = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) encabezado.createCell(i).setCellValue(headers[i]);
            var fila = sheet.createRow(1);
            for (int i = 0; i < datos.length; i++) fila.createCell(i).setCellValue(datos[i]);
            wb.write(out);
            return new MockMultipartFile("archivo", "estudiantes.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    out.toByteArray());
        }
    }
}
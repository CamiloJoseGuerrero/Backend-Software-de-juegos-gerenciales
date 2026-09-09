package com.estratego.application.usecase;

import com.estratego.application.dto.auth.UsuarioResponse;
import com.estratego.application.dto.docente.CargaMasivaResponse;
import com.estratego.application.mapper.UsuarioMapper;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CargaMasivaEstudiantesServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private CargaMasivaEstudiantesService service;

    @Test
    void procesaEstudianteGeneraContrasenaYAsignaRolEstudiante() throws IOException {
        MockMultipartFile archivo = crearArchivo(
                new String[]{"Ana Perez", "ANA@correo.com", "123456"}
        );
        Usuario guardado = new Usuario(1L, "Ana Perez", "ana@correo.com", "123456", "hash", Rol.ESTUDIANTE);
        UsuarioResponse response = new UsuarioResponse(1L, "Ana Perez", "ana@correo.com", "123456", "ESTUDIANTE");

        when(usuarioRepository.existsByCorreo("ana@correo.com")).thenReturn(false);
        when(usuarioRepository.existsByNumeroIdentificacion("123456")).thenReturn(false);
        when(passwordEncoder.encode("USU-001-123456")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(guardado);
        when(usuarioMapper.toResponse(guardado)).thenReturn(response);

        CargaMasivaResponse resultado = service.procesar(archivo);

        assertEquals(1, resultado.getCreados().size());
        assertTrue(resultado.getErrores().isEmpty());
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals(Rol.ESTUDIANTE, captor.getValue().getRol());
        assertEquals("hash", captor.getValue().getContrasena());
        verify(passwordEncoder).encode("USU-001-123456");
    }

    @Test
    void informaCorreoDuplicadoSinCrearUsuario() throws IOException {
        MockMultipartFile archivo = crearArchivo(
                new String[]{"Ana Perez", "ana@correo.com", "123456"}
        );
        when(usuarioRepository.existsByCorreo("ana@correo.com")).thenReturn(true);

        CargaMasivaResponse resultado = service.procesar(archivo);

        assertTrue(resultado.getCreados().isEmpty());
        assertEquals(1, resultado.getErrores().size());
        assertEquals("El correo ya está registrado", resultado.getErrores().get(0).getMensaje());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    private MockMultipartFile crearArchivo(String[] datos) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Estudiantes");
            var encabezado = sheet.createRow(0);
            encabezado.createCell(0).setCellValue("nombre");
            encabezado.createCell(1).setCellValue("correo");
            encabezado.createCell(2).setCellValue("numeroIdentificacion");
            var fila = sheet.createRow(1);
            for (int index = 0; index < datos.length; index++) {
                fila.createCell(index).setCellValue(datos[index]);
            }
            workbook.write(output);
            return new MockMultipartFile(
                    "archivo",
                    "estudiantes.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    output.toByteArray()
            );
        }
    }
}

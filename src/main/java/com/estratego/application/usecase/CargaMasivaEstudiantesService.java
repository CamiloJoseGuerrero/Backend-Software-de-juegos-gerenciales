package com.estratego.application.usecase;

import com.estratego.application.dto.auth.UsuarioResponse;
import com.estratego.application.dto.docente.CargaMasivaResponse;
import com.estratego.application.dto.docente.ErrorCargaResponse;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CargaMasivaEstudiantesService {

    private static final int EXPECTED_COLUMNS = 3;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.estratego.application.mapper.UsuarioMapper usuarioMapper;

    public CargaMasivaResponse procesar(MultipartFile file) {
        validateFile(file);
        List<UsuarioResponse> creados = new ArrayList<>();
        List<ErrorCargaResponse> errores = new ArrayList<>();
        Set<String> correosDelArchivo = new HashSet<>();
        Set<String> identificacionesDelArchivo = new HashSet<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            boolean headerSkipped = false;

            for (Row row : sheet) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                if (isEmpty(row, formatter)) {
                    continue;
                }

                int fila = row.getRowNum() + 1;
                String nombre = readCell(row, 0, formatter);
                String correo = readCell(row, 1, formatter).toLowerCase(Locale.ROOT);
                String identificacion = readCell(row, 2, formatter);
                String error = validateRow(row, nombre, correo, identificacion, fila,
                        correosDelArchivo, identificacionesDelArchivo);

                if (error != null) {
                    errores.add(new ErrorCargaResponse(fila, correo, identificacion, error));
                    continue;
                }

                Usuario usuario = new Usuario(
                        null,
                        nombre,
                        correo,
                        identificacion,
                        passwordEncoder.encode("USU-001-" + identificacion),
                        Rol.ESTUDIANTE
                );
                Usuario guardado = usuarioRepository.save(usuario);
                creados.add(usuarioMapper.toResponse(guardado));
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("No se pudo leer el archivo Excel", ex);
        }

        return new CargaMasivaResponse(creados, errores);
    }

    private String validateRow(Row row, String nombre, String correo, String identificacion, int fila,
                               Set<String> correosDelArchivo, Set<String> identificacionesDelArchivo) {
        if (row.getLastCellNum() < EXPECTED_COLUMNS) {
            return "La fila debe contener nombre, correo y número de identificación";
        }
        if (nombre.isBlank() || correo.isBlank() || identificacion.isBlank()) {
            return "Nombre, correo y número de identificación son obligatorios";
        }
        if (!correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "El correo debe ser válido";
        }
        if (!correosDelArchivo.add(correo)) {
            return "El correo está repetido en el archivo";
        }
        if (!identificacionesDelArchivo.add(identificacion)) {
            return "El número de identificación está repetido en el archivo";
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            return "El correo ya está registrado";
        }
        if (usuarioRepository.existsByNumeroIdentificacion(identificacion)) {
            return "El número de identificación ya está registrado";
        }
        return null;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debe adjuntar un archivo Excel");
        }
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        if (!"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)
                && (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx"))) {
            throw new IllegalArgumentException("El archivo debe tener formato .xlsx");
        }
    }

    private boolean isEmpty(Row row, DataFormatter formatter) {
        for (int index = 0; index < EXPECTED_COLUMNS; index++) {
            if (!readCell(row, index, formatter).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String readCell(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }
}
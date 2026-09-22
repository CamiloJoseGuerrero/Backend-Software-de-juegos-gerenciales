package com.estratego.application.usecase;

import com.estratego.application.dto.docente.CargaMasivaResponse;
import com.estratego.application.dto.docente.CreadoEstudianteResponse;
import com.estratego.application.dto.docente.ErrorCargaResponse;
import com.estratego.domain.model.usuario.Estudiante;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.EstudianteRepository;
import com.estratego.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CargaMasivaEstudiantesService {

    private static final String PREFIJO_PASSWORD = "Usu-001-";
    private static final String SUFIJO_PASSWORD = "!";

    private static final Set<String> HEADERS_CORREO = Set.of("correo", "correo electronico", "email", "e-mail");
    private static final Set<String> HEADERS_NOMBRE = Set.of("nombre", "nombre completo", "nombres");
    private static final Set<String> HEADERS_ID = Set.of(
            "numeroidentificacion", "numero identificacion", "numero de identificacion",
            "identificacion", "cedula", "documento"
    );
    private static final Set<String> HEADERS_EDAD = Set.of("edad");
    private static final Set<String> HEADERS_GENERO = Set.of("genero", "sexo");

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CargaMasivaResponse procesar(MultipartFile file, String correoDocente) {
        validateFile(file);

        List<CreadoEstudianteResponse> creados = new ArrayList<>();
        List<ErrorCargaResponse> errores = new ArrayList<>();
        Set<String> correosDelArchivo = new HashSet<>();
        Set<String> identificacionesDelArchivo = new HashSet<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> columnas = detectarColumnas(headerRow, formatter);

            Integer idxCorreo = columnas.get("correo");
            Integer idxNombre = columnas.get("nombre");
            Integer idxId = columnas.get("id");
            Integer idxEdad = columnas.get("edad");
            Integer idxGenero = columnas.get("genero");

            if (idxCorreo == null || idxNombre == null || idxId == null) {
                throw new IllegalArgumentException(
                        "El archivo debe contener: correo, nombre, numero de identificacion. " +
                        "Headers encontrados: " + columnas.keySet());
            }

            int primeraFilaDatos = sheet.getFirstRowNum() + 1;
            for (int i = primeraFilaDatos; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String correo = readCell(row, idxCorreo, formatter).toLowerCase(Locale.ROOT);
                String nombre = readCell(row, idxNombre, formatter);
                String identificacion = readCell(row, idxId, formatter);
                String edadRaw = idxEdad != null ? readCell(row, idxEdad, formatter) : "";
                String generoRaw = idxGenero != null ? readCell(row, idxGenero, formatter) : "";

                if (correo.isBlank() && nombre.isBlank() && identificacion.isBlank()
                        && edadRaw.isBlank() && generoRaw.isBlank()) continue;

                int fila = i + 1;
                String error = validateRow(nombre, correo, identificacion, edadRaw, generoRaw,
                        correosDelArchivo, identificacionesDelArchivo);

                if (error != null) {
                    errores.add(new ErrorCargaResponse(fila, correo, identificacion, error));
                    continue;
                }

                Integer edad = parseEdad(edadRaw);
                String genero = generoRaw.isBlank() ? null : generoRaw.toUpperCase(Locale.ROOT);

                try {
                    String passwordPlano = PREFIJO_PASSWORD + identificacion + SUFIJO_PASSWORD;
                    String usuario = generarUsuarioDesdeCorreo(correo);

                    Usuario usuarioObj = new Usuario(
                            null, nombre, correo, identificacion, usuario,
                            passwordEncoder.encode(passwordPlano), Rol.ESTUDIANTE
                    );
                    Usuario guardado = usuarioRepository.save(usuarioObj);

                    Estudiante estudiante = new Estudiante(
                            guardado.getId(), null, null, null, edad, genero
                    );
                    estudianteRepository.save(estudiante);

                    creados.add(new CreadoEstudianteResponse(
                            guardado.getId(), guardado.getNombre(), guardado.getCorreo(),
                            guardado.getNumeroIdentificacion(), edad, genero,
                            guardado.getRol().name(), passwordPlano
                    ));
                } catch (DataIntegrityViolationException ex) {
                    errores.add(new ErrorCargaResponse(fila, correo, identificacion,
                            "El correo o la identificación ya existen en la base de datos"));
                    correosDelArchivo.remove(correo);
                    identificacionesDelArchivo.remove(identificacion);
                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("No se pudo leer el archivo Excel", ex);
        }

        if (creados.isEmpty() && errores.isEmpty()) {
            throw new IllegalArgumentException("El archivo no contiene filas de datos");
        }

        return new CargaMasivaResponse(creados, errores);
    }

    private String generarUsuarioDesdeCorreo(String correo) {
        String base = correo.substring(0, correo.indexOf('@'))
                .replaceAll("[^a-z0-9.]", ".")
                .toLowerCase(Locale.ROOT);
        String usuario = base;
        int sufijo = 1;
        while (usuarioRepository.existsByUsuario(usuario)) {
            usuario = base + sufijo++;
        }
        return usuario;
    }

    private Map<String, Integer> detectarColumnas(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> columnas = new HashMap<>();
        if (headerRow == null) return columnas;

        short lastCell = headerRow.getLastCellNum();
        for (int i = 0; i < lastCell; i++) {
            String raw = readCell(headerRow, i, formatter);
            if (raw.isBlank()) continue;
            String normalized = normalizarHeader(raw);

            if (HEADERS_CORREO.contains(normalized)) columnas.put("correo", i);
            else if (HEADERS_NOMBRE.contains(normalized)) columnas.put("nombre", i);
            else if (HEADERS_ID.contains(normalized)) columnas.put("id", i);
            else if (HEADERS_EDAD.contains(normalized)) columnas.put("edad", i);
            else if (HEADERS_GENERO.contains(normalized)) columnas.put("genero", i);
        }
        return columnas;
    }

    private String normalizarHeader(String raw) {
        String sinTildes = Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinTildes.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String validateRow(String nombre, String correo, String identificacion,
                               String edadRaw, String generoRaw,
                               Set<String> correosDelArchivo, Set<String> identificacionesDelArchivo) {
        if (nombre.isBlank() || correo.isBlank() || identificacion.isBlank())
            return "Nombre, correo y número de identificación son obligatorios";
        if (!correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            return "El correo debe ser válido";
        if (!correosDelArchivo.add(correo)) return "El correo está repetido en el archivo";
        if (!identificacionesDelArchivo.add(identificacion))
            return "El número de identificación está repetido en el archivo";
        if (usuarioRepository.existsByCorreo(correo)) return "El correo ya está registrado";
        if (usuarioRepository.existsByNumeroIdentificacion(identificacion))
            return "El número de identificación ya está registrado";
        if (!edadRaw.isBlank()) {
            try {
                int edad = Integer.parseInt(edadRaw);
                if (edad <= 0 || edad > 120) return "La edad debe estar entre 1 y 120";
            } catch (NumberFormatException e) {
                return "La edad debe ser un número entero";
            }
        }
        if (!generoRaw.isBlank()) {
            String g = generoRaw.toUpperCase(Locale.ROOT);
            if (!g.equals("M") && !g.equals("F")) return "El género debe ser M o F";
        }
        return null;
    }

    private Integer parseEdad(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try { return Integer.parseInt(raw.trim()); } catch (NumberFormatException e) { return null; }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("Debe adjuntar un archivo Excel");
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        if (!"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)
                && (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx"))) {
            throw new IllegalArgumentException("El archivo debe tener formato .xlsx");
        }
    }

    private String readCell(Row row, int index, DataFormatter formatter) {
        if (index < 0 || row == null) return "";
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }
}
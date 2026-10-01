-- Vínculo docente ↔ estudiante (PostgreSQL / Neon)
-- Cada docente ve solo los estudiantes que él cargó (manual o Excel).
-- Un mismo estudiante puede estar vinculado a varios docentes (p. ej. dos cursos).
-- Ejecutar ANTES de arrancar el backend: con ddl-auto=validate, sin esta tabla la app no inicia.

BEGIN;

-- 1. Tabla de vínculo
CREATE TABLE IF NOT EXISTS docente_estudiante (
    id_docente_estudiante BIGSERIAL PRIMARY KEY,
    id_docente            BIGINT    NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    id_estudiante         BIGINT    NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    fecha_vinculo         TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_docente_estudiante UNIQUE (id_docente, id_estudiante),
    CONSTRAINT ck_docente_estudiante_distintos CHECK (id_docente <> id_estudiante)
);

CREATE INDEX IF NOT EXISTS ix_docente_estudiante_estudiante
    ON docente_estudiante (id_estudiante);

-- 2. Estudiantes que ya existen: se asignan al docente dueño de las
--    simulaciones donde son integrantes. Los que no están en ninguna
--    simulación quedan sin docente (hay que volver a cargarlos).
INSERT INTO docente_estudiante (id_docente, id_estudiante)
SELECT DISTINCT s.id_usuario_coordinador, i.id_usuario
FROM integrante i
JOIN empresa    e ON e.id_empresa    = i.id_empresa
JOIN simulacion s ON s.id_simulacion = e.id_simulacion
JOIN usuarios   u ON u.id            = i.id_usuario
WHERE u.rol = 'ESTUDIANTE'
ON CONFLICT (id_docente, id_estudiante) DO NOTHING;

COMMIT;

-- Verificación:
-- SELECT d.correo AS docente, COUNT(*) AS estudiantes
-- FROM docente_estudiante de JOIN usuarios d ON d.id = de.id_docente
-- GROUP BY d.correo;
--
-- Estudiantes que quedaron sin docente:
-- SELECT u.id, u.nombre, u.correo FROM usuarios u
-- WHERE u.rol = 'ESTUDIANTE'
--   AND NOT EXISTS (SELECT 1 FROM docente_estudiante de WHERE de.id_estudiante = u.id);

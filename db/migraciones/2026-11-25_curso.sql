-- Cursos del docente (PostgreSQL / Neon)
-- Permite organizar estudiantes y simulaciones por curso, y restringir la
-- formación de empresas de una simulación a los estudiantes de su curso.
-- Ejecutar ANTES de arrancar el backend (con ddl-auto=validate).

BEGIN;

-- 1. Tabla de cursos
CREATE TABLE IF NOT EXISTS curso (
    id_curso       BIGSERIAL    PRIMARY KEY,
    id_docente     BIGINT       NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nombre         VARCHAR(150) NOT NULL,
    fecha_creacion TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_curso_docente
    ON curso (id_docente);

-- 2. Vínculo curso ↔ estudiante (un estudiante puede pertenecer a varios cursos)
CREATE TABLE IF NOT EXISTS curso_estudiante (
    id_curso_estudiante BIGSERIAL PRIMARY KEY,
    id_curso            BIGINT    NOT NULL REFERENCES curso(id_curso) ON DELETE CASCADE,
    id_estudiante       BIGINT    NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    fecha_vinculo       TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_curso_estudiante UNIQUE (id_curso, id_estudiante)
);

CREATE INDEX IF NOT EXISTS ix_curso_estudiante_estudiante
    ON curso_estudiante (id_estudiante);

-- 3. Columna opcional id_curso en simulacion (nullable para retrocompatibilidad)
ALTER TABLE simulacion
    ADD COLUMN IF NOT EXISTS id_curso BIGINT REFERENCES curso(id_curso) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS ix_simulacion_curso
    ON simulacion (id_curso);

COMMIT;

-- Integrante: marca de líder por empresa (PostgreSQL / Neon)
-- Ejecutar ANTES de arrancar el backend: con ddl-auto=validate, sin es_lider la app no inicia.
-- Nota: el UNIQUE (id_empresa, id_usuario) ya existe como uq_empresa_usuario; no se recrea.

BEGIN;

-- 1. Columna es_lider
ALTER TABLE integrante
    ADD COLUMN IF NOT EXISTS es_lider BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Solo un líder por empresa
CREATE UNIQUE INDEX IF NOT EXISTS ux_integrante_un_lider_por_empresa
    ON integrante (id_empresa)
    WHERE es_lider;

-- 3. (Opcional) borrar integrantes si se borra el usuario
ALTER TABLE integrante
    DROP CONSTRAINT IF EXISTS fk_integrante_usuario;
ALTER TABLE integrante
    ADD CONSTRAINT fk_integrante_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE;

COMMIT;

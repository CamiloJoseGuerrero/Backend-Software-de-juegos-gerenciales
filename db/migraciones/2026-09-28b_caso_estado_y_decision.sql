-- Casos: estado (borrador/activo), asignación de equipos y decisión del líder por empresa (PostgreSQL / Neon)
-- Ejecutar ANTES de arrancar el backend con este código (ddl-auto=validate).
-- Requiere haber ejecutado antes 2026-09-28_caso.sql.

BEGIN;

ALTER TABLE caso
    ADD COLUMN IF NOT EXISTS estado             VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    ADD COLUMN IF NOT EXISTS asignacion_equipos VARCHAR(20) NOT NULL DEFAULT 'MANUAL';

-- Solo un caso ACTIVO por simulación
CREATE UNIQUE INDEX IF NOT EXISTS ux_caso_un_activo_por_simulacion
    ON caso (id_simulacion)
    WHERE estado = 'ACTIVO';

-- Decisión de una empresa en un caso (la toma el líder). Permanente: una sola por empresa y caso.
-- No se usa la tabla "decision" existente porque esa apunta a periodo, no a caso.
CREATE TABLE IF NOT EXISTS decision_caso (
    id_decision_caso  BIGSERIAL PRIMARY KEY,
    id_caso           BIGINT    NOT NULL,
    id_empresa        BIGINT    NOT NULL,
    id_opcion         BIGINT    NOT NULL,
    id_usuario        BIGINT    NOT NULL,
    fecha_decision    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_decision_caso_caso    FOREIGN KEY (id_caso)    REFERENCES caso(id_caso)         ON DELETE CASCADE,
    CONSTRAINT fk_decision_caso_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa)   ON DELETE CASCADE,
    CONSTRAINT fk_decision_caso_opcion  FOREIGN KEY (id_opcion)  REFERENCES caso_opcion(id_opcion),
    CONSTRAINT fk_decision_caso_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id),
    CONSTRAINT uq_decision_caso_empresa UNIQUE (id_caso, id_empresa)
);

COMMIT;

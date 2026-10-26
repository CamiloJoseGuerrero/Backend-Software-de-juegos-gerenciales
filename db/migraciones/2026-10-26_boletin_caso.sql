-- Boletín de factores de entorno macroeconómico por caso (D14)
-- Requiere 2026-09-28_caso.sql
-- Cada caso de una simulación tiene a lo sumo un boletín (1:1 opcional).
-- Si el caso se borra, su boletín se elimina en cascada.

BEGIN;

CREATE TABLE IF NOT EXISTS boletin_caso (
    id_boletin_caso    BIGSERIAL      PRIMARY KEY,
    id_caso            BIGINT         NOT NULL UNIQUE REFERENCES caso(id_caso) ON DELETE CASCADE,
    inflacion          NUMERIC(8,2)   NOT NULL,
    tasa_interes       NUMERIC(8,2)   NOT NULL,
    tasa_cambio        NUMERIC(12,2)  NOT NULL,
    nota               TEXT,
    publicado_en       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_boletin_caso_id_caso ON boletin_caso(id_caso);

COMMIT;

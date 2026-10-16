-- Estado financiero por empresa: porcentaje de penalización sorteado (PostgreSQL / Neon)
-- La empresa que no decide recibe un porcentaje al azar entre penalizacion_min y penalizacion_max del caso,
-- sorteado UNA vez al cerrar el caso y guardado aquí (no se recalcula). Solo en los FINAL con origen PENALIZACION.
-- Ejecutar ANTES de arrancar el backend con este código (ddl-auto=validate).

BEGIN;

ALTER TABLE estado_financiero_empresa ADD COLUMN IF NOT EXISTS penalizacion_porcentaje NUMERIC(5,2);

ALTER TABLE estado_financiero_empresa DROP CONSTRAINT IF EXISTS ck_efe_penalizacion;
ALTER TABLE estado_financiero_empresa ADD CONSTRAINT ck_efe_penalizacion CHECK (
    (origen = 'PENALIZACION') OR penalizacion_porcentaje IS NULL
);

-- Los FINAL penalizados que ya existen se calcularon con el máximo del caso
UPDATE estado_financiero_empresa f
SET penalizacion_porcentaje = c.penalizacion_max
FROM caso c
WHERE c.id_caso = f.id_caso
  AND f.origen = 'PENALIZACION'
  AND f.penalizacion_porcentaje IS NULL;

COMMIT;

-- Verificación:
-- SELECT origen, COUNT(*), MIN(penalizacion_porcentaje), MAX(penalizacion_porcentaje)
-- FROM estado_financiero_empresa GROUP BY origen;

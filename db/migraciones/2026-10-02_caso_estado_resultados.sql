-- Caso: datos del Estado de Resultados (PostgreSQL / Neon)
-- ventas netas, costo de ventas y gastos operativos, para que el estudiante vea
-- de dónde sale la utilidad (utilidad bruta y operativa las calcula el frontend).
-- Ejecutar ANTES de arrancar el backend con este código (ddl-auto=validate).
-- Las columnas admiten NULL solo por los casos que ya existen; la API las exige en casos nuevos o editados.

BEGIN;

ALTER TABLE caso
    ADD COLUMN IF NOT EXISTS ventas_netas      NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS costo_ventas      NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS gastos_operativos NUMERIC(18,2);

ALTER TABLE caso DROP CONSTRAINT IF EXISTS ck_caso_estado_resultados_no_negativos;
ALTER TABLE caso ADD CONSTRAINT ck_caso_estado_resultados_no_negativos
    CHECK ((ventas_netas IS NULL OR ventas_netas >= 0)
       AND (costo_ventas IS NULL OR costo_ventas >= 0)
       AND (gastos_operativos IS NULL OR gastos_operativos >= 0));

COMMIT;

-- Verificación:
-- SELECT column_name, data_type, is_nullable FROM information_schema.columns
-- WHERE table_name = 'caso' AND column_name IN ('ventas_netas','costo_ventas','gastos_operativos');

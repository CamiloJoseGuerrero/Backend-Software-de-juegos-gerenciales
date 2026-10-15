-- Caso: totales del balance opcionales (D2) (PostgreSQL / Neon)
-- Solo el primer caso de la simulación trae balance; los siguientes solo traen estado de resultados,
-- así que activo_total, pasivo_total y patrimonio quedan NULL en ellos. utilidad_neta sigue obligatoria.
-- Ejecutar ANTES de crear casos que no sean el primero (si no, la base responde 409).

BEGIN;

ALTER TABLE caso ALTER COLUMN activo_total DROP;
ALTER TABLE caso ALTER COLUMN pasivo_total DROP;
ALTER TABLE caso ALTER COLUMN patrimonio   DROP;

COMMIT;

-- Verificación (las tres deben salir con is_nullable = YES):
-- SELECT column_name, is_nullable FROM information_schema.columns
-- WHERE table_name = 'caso' AND column_name IN ('activo_total', 'pasivo_total', 'patrimonio', 'utilidad_neta');

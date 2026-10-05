-- Caso: cuándo se activó por primera vez (PostgreSQL / Neon)
-- La clasificación solo cuenta casos que se activaron alguna vez; un caso que se quedó
-- en borrador con fechas pasadas no debe penalizar a las empresas.
-- Ejecutar ANTES de arrancar el backend con este código (ddl-auto=validate).

BEGIN;

ALTER TABLE caso ADD COLUMN IF NOT EXISTS activado_en TIMESTAMP;

-- Casos existentes: los activos hoy o con alguna decisión se dan por activados
UPDATE caso c
SET activado_en = c.fecha_inicio
WHERE c.activado_en IS NULL
  AND (c.estado = 'ACTIVO'
       OR EXISTS (SELECT 1 FROM decision_caso d WHERE d.id_caso = c.id_caso));

COMMIT;

-- Verificación:
-- SELECT id_caso, estado, activado_en FROM caso ORDER BY id_caso DESC LIMIT 10;

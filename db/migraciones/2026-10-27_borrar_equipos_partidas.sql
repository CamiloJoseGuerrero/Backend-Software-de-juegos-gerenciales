-- =====================================================================
-- Retiro de equipos y partidas, fase 2 (PostgreSQL / Neon) - 2026-10-27
--
-- Contexto: el modelo viejo (equipos + partidas) se reemplazo por
-- empresas + integrantes y simulaciones + casos. En la fase 1 (commit 5638eb2,
-- 2026-10-20) se quito todo el codigo que usaba estas tablas; desde entonces
-- /docente/equipos y /docente/partidas responden 404 y nada las lee ni escribe.
-- Esta migracion borra las 4 tablas que quedaron huerfanas:
--   partida_equipos, equipo_estudiantes, partidas, equipos
--
-- NO toca: caso.asignacion_equipos, las fechas de partida del caso
-- (fecha_inicio / fecha_fin) ni ninguna otra tabla del modelo actual.
--
-- Uso:
--   0. (Recomendado) En Neon cree una branch de respaldo antes de correrla.
--   1. Correr TODO tal cual: termina en ROLLBACK, asi que no borra nada.
--      Revise el paso 1 (filas que se pierden) y que el paso 2 no devuelva filas.
--   2. Cambiar el ROLLBACK del final por COMMIT y volver a correr.
--   3. Reiniciar el backend (ddl-auto=validate no revisa tablas que sobran,
--      asi que arranca igual antes y despues).
-- Los DROP van sin CASCADE: si alguna otra tabla todavia apunta a estas,
-- falla la transaccion entera y no se borra nada.
-- =====================================================================

BEGIN;

-- 1. Filas que se pierden (solo informativo) --------------------------
SELECT 'equipos'            AS tabla, COUNT(*) AS filas FROM equipos
UNION ALL SELECT 'equipo_estudiantes', COUNT(*) FROM equipo_estudiantes
UNION ALL SELECT 'partidas',           COUNT(*) FROM partidas
UNION ALL SELECT 'partida_equipos',    COUNT(*) FROM partida_equipos;

-- 2. Llaves foraneas de OTRAS tablas que apunten a estas (debe salir vacio)
SELECT conrelid::regclass AS tabla_que_apunta, conname AS restriccion, confrelid::regclass AS apunta_a
FROM pg_constraint
WHERE contype = 'f'
  AND confrelid IN ('equipos'::regclass, 'partidas'::regclass)
  AND conrelid NOT IN ('equipo_estudiantes'::regclass, 'partida_equipos'::regclass, 'partidas'::regclass);

-- 3. Borrado (primero las tablas hijas) --------------------------------
DROP TABLE partida_equipos;
DROP TABLE equipo_estudiantes;
DROP TABLE partidas;
DROP TABLE equipos;

-- 4. Verificacion: no debe quedar ninguna de las 4 -------------------
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('equipos', 'equipo_estudiantes', 'partidas', 'partida_equipos');

ROLLBACK;   -- cambiar por COMMIT cuando el resultado este bien

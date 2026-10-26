-- =====================================================================
-- Limpieza de datos de prueba (Neon) — REVISAR ANTES DE CORRER
--
-- Borra:
--   * Simulaciones de los scripts ("Prueba script ...", "Prueba estudiantes ...")
--     y todas las creadas por docentes de prueba (@estratego-test.local),
--     con sus empresas, integrantes, casos, opciones y decisiones.
--   * Usuarios @estratego-test.local (docentes B, estudiantes de prueba, integracion.*)
--     con sus vínculos docente-estudiante y datos de estudiante/profesor.
--
-- NO borra:
--   * La simulación 45 ("Prueba clasificación (auto)"), ni los usuarios que participan en ella.
--   * Los estudiantes de los Excel (@correo.edu.co) ni los docentes reales.
--
-- Uso recomendado:
--   1. Correr TODO tal cual: termina en ROLLBACK, así que solo muestra qué borraría.
--   2. Revisar los conteos del paso 2.
--   3. Cambiar el ROLLBACK del final por COMMIT y volver a correr.
-- Si algo falla (p. ej. una tabla que referencia a estos usuarios y no está aquí),
-- la transacción entera se cancela y no se borra nada.
-- =====================================================================

BEGIN;

-- 1. Qué se borra -----------------------------------------------------
CREATE TEMP TABLE sims_borrar ON COMMIT DROP AS
SELECT s.id_simulacion
FROM simulacion s
WHERE s.id_simulacion <> 45
  AND (   s.nombre_curso LIKE 'Prueba script %'
       OR s.nombre_curso LIKE 'Prueba estudiantes %'
       OR s.id_usuario_coordinador IN (SELECT id FROM usuarios WHERE correo LIKE '%@estratego-test.local'));

CREATE TEMP TABLE usuarios_borrar ON COMMIT DROP AS
SELECT u.id
FROM usuarios u
WHERE u.correo LIKE '%@estratego-test.local'
  -- se conservan los que coordinan o participan en una simulación que se queda (p. ej. la 45)
  AND u.id NOT IN (SELECT s.id_usuario_coordinador FROM simulacion s
                   WHERE s.id_simulacion NOT IN (SELECT id_simulacion FROM sims_borrar))
  AND u.id NOT IN (SELECT i.id_usuario FROM integrante i
                   JOIN empresa e ON e.id_empresa = i.id_empresa
                   WHERE e.id_simulacion NOT IN (SELECT id_simulacion FROM sims_borrar));

-- 2. Vista previa -----------------------------------------------------
SELECT 'simulaciones' AS que, COUNT(*) AS cuantas FROM sims_borrar
UNION ALL SELECT 'empresas',  COUNT(*) FROM empresa WHERE id_simulacion IN (SELECT id_simulacion FROM sims_borrar)
UNION ALL SELECT 'casos',     COUNT(*) FROM caso    WHERE id_simulacion IN (SELECT id_simulacion FROM sims_borrar)
UNION ALL SELECT 'usuarios',  COUNT(*) FROM usuarios_borrar
UNION ALL SELECT 'usuarios que se conservan (@estratego-test.local)',
                 COUNT(*) FROM usuarios WHERE correo LIKE '%@estratego-test.local'
                                          AND id NOT IN (SELECT id FROM usuarios_borrar);

-- 3. Simulaciones y todo lo que cuelga de ellas ----------------------
DELETE FROM decision_caso
WHERE id_caso IN (SELECT id_caso FROM caso WHERE id_simulacion IN (SELECT id_simulacion FROM sims_borrar))
   OR id_usuario IN (SELECT id FROM usuarios_borrar);

DELETE FROM caso_opcion
WHERE id_caso IN (SELECT id_caso FROM caso WHERE id_simulacion IN (SELECT id_simulacion FROM sims_borrar));

DELETE FROM caso WHERE id_simulacion IN (SELECT id_simulacion FROM sims_borrar);

DELETE FROM integrante
WHERE id_empresa IN (SELECT id_empresa FROM empresa WHERE id_simulacion IN (SELECT id_simulacion FROM sims_borrar))
   OR id_usuario IN (SELECT id FROM usuarios_borrar);

DELETE FROM empresa WHERE id_simulacion IN (SELECT id_simulacion FROM sims_borrar);

DELETE FROM simulacion WHERE id_simulacion IN (SELECT id_simulacion FROM sims_borrar);

-- 4. (Equipos y partidas: tablas borradas en la fase 2, migracion 2026-10-27)

-- 5. Usuarios ---------------------------------------------------------
DELETE FROM docente_estudiante
WHERE id_docente IN (SELECT id FROM usuarios_borrar) OR id_estudiante IN (SELECT id FROM usuarios_borrar);
DELETE FROM estudiante WHERE id_estudiante IN (SELECT id FROM usuarios_borrar);
DELETE FROM profesor   WHERE id_profesor   IN (SELECT id FROM usuarios_borrar);
DELETE FROM usuarios   WHERE id            IN (SELECT id FROM usuarios_borrar);

-- 6. Resultado: cambiar por COMMIT cuando la vista previa se vea bien
ROLLBACK;

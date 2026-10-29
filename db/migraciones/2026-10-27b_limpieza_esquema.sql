-- =====================================================================
-- Limpieza del esquema (PostgreSQL / Neon) - 2026-10-27
-- Requiere: 2026-10-27_borrar_equipos_partidas.sql ya aplicada (con COMMIT).
--
-- Que hace (todo en una transaccion: si algo falla, no cambia nada):
--   1. Borra la vista vw_resumen_simulacion y 5 tablas del diseno original que el codigo nunca uso (todas vacias):
--      boletin, factor_entorno, informe_periodo, decision, periodo.
--      Las reemplazaron boletin_caso, decision_caso, estado_financiero_empresa y caso.
--   2. Borra 4 indices repetidos (cada uno duplica un UNIQUE que ya existe).
--   3. Copia edad/genero de usuarios a estudiante (si estudiante no los tiene)
--      y borra de usuarios las columnas viejas docente_id, edad y genero
--      (el codigo no las usa: la relacion docente-estudiante esta en
--      docente_estudiante y edad/genero en estudiante).
--   4. Une estudiante y profesor a usuarios con llave foranea (hoy no la tienen):
--      primero borra las filas huerfanas (sin usuario).
--   5. Agrega CHECK a los estados que no tenian: usuarios.rol, caso.estado,
--      caso.asignacion_equipos, empresa.estado, integrante.departamento.
--
-- Uso:
--   0. (Recomendado) Branch de respaldo en Neon.
--   IMPORTANTE: seleccionar TODO el archivo (Ctrl+A) para que entre el BEGIN;
--   si falta el BEGIN, cada sentencia se guarda sola y no hay ROLLBACK.
--   1. Correr TODO tal cual: termina en ROLLBACK, no cambia nada.
--      Revisar el paso 0 (diagnostico): si alguna fila de "valores_invalidos" sale > 0,
--      PARAR y avisar (el CHECK fallaria).
--   2. Cambiar el ROLLBACK del final por COMMIT y volver a correr.
--   3. Reiniciar el backend (ddl-auto=validate: estas tablas/columnas no las usa).
-- =====================================================================

BEGIN;

-- 0. Diagnostico (solo informativo) -----------------------------------
SELECT 'tablas viejas que quedan' AS que,
       (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public'
          AND table_name IN ('boletin','factor_entorno','informe_periodo','decision','periodo')) AS cantidad
UNION ALL SELECT 'usuarios con docente_id', COUNT(*) FROM usuarios WHERE docente_id IS NOT NULL
UNION ALL SELECT 'usuarios con edad o genero', COUNT(*) FROM usuarios WHERE edad IS NOT NULL OR genero IS NOT NULL
UNION ALL SELECT 'estudiante huerfano (sin usuario)', COUNT(*) FROM estudiante e WHERE NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.id = e.id_estudiante)
UNION ALL SELECT 'profesor huerfano (sin usuario)', COUNT(*) FROM profesor p WHERE NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.id = p.id_profesor)
UNION ALL SELECT 'valores_invalidos usuarios.rol', COUNT(*) FROM usuarios WHERE rol NOT IN ('DOCENTE','ESTUDIANTE')
UNION ALL SELECT 'valores_invalidos caso.estado', COUNT(*) FROM caso WHERE estado NOT IN ('BORRADOR','ACTIVO')
UNION ALL SELECT 'valores_invalidos caso.asignacion_equipos', COUNT(*) FROM caso WHERE asignacion_equipos NOT IN ('MANUAL','AUTOMATICA')
UNION ALL SELECT 'valores_invalidos empresa.estado', COUNT(*) FROM empresa WHERE estado IS NOT NULL AND estado NOT IN ('ACTIVA','INACTIVA','CERRADA')
UNION ALL SELECT 'valores_invalidos integrante.departamento', COUNT(*) FROM integrante WHERE departamento IS NOT NULL AND departamento NOT IN ('GERENCIA_GENERAL','COMERCIAL','OPERACIONES','ADMINISTRATIVA');

-- 1. Vista y tablas del diseno original -------------------------------
-- vw_resumen_simulacion juntaba simulacion/empresa con periodo, factor_entorno,
-- decision e informe_periodo (siempre vacias). Nadie la usa (ni el backend ni scripts).
DROP VIEW IF EXISTS vw_resumen_simulacion;

DROP TABLE IF EXISTS boletin;
DROP TABLE IF EXISTS factor_entorno;
DROP TABLE IF EXISTS informe_periodo;
DROP TABLE IF EXISTS decision;
DROP TABLE IF EXISTS periodo;

-- 2. Indices repetidos -------------------------------------------------
DROP INDEX IF EXISTS idx_boletin_caso_id_caso;            -- lo cubre boletin_caso_id_caso_key
DROP INDEX IF EXISTS idx_usuarios_correo;                 -- lo cubre usuarios_correo_key
DROP INDEX IF EXISTS idx_usuarios_numero_identificacion;  -- lo cubre usuarios_numero_identificacion_key
DROP INDEX IF EXISTS idx_integrante_empresa;              -- lo cubre uq_empresa_usuario (empieza por id_empresa)

-- 3. Columnas viejas de usuarios (sin perder edad/genero) --------------
UPDATE estudiante e
SET edad   = COALESCE(e.edad, u.edad),
    genero = COALESCE(e.genero, u.genero)
FROM usuarios u
WHERE u.id = e.id_estudiante
  AND (u.edad IS NOT NULL OR u.genero IS NOT NULL);

ALTER TABLE usuarios DROP COLUMN docente_id;
ALTER TABLE usuarios DROP COLUMN edad;
ALTER TABLE usuarios DROP COLUMN genero;

-- 4. estudiante y profesor -> usuarios ---------------------------------
DELETE FROM estudiante e WHERE NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.id = e.id_estudiante);
DELETE FROM profesor   p WHERE NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.id = p.id_profesor);

ALTER TABLE estudiante ADD CONSTRAINT fk_estudiante_usuario
    FOREIGN KEY (id_estudiante) REFERENCES usuarios(id) ON DELETE CASCADE;
ALTER TABLE profesor ADD CONSTRAINT fk_profesor_usuario
    FOREIGN KEY (id_profesor) REFERENCES usuarios(id) ON DELETE CASCADE;

-- 5. CHECK de estados --------------------------------------------------
ALTER TABLE usuarios   ADD CONSTRAINT ck_usuarios_rol
    CHECK (rol IN ('DOCENTE','ESTUDIANTE'));
ALTER TABLE caso       ADD CONSTRAINT ck_caso_estado
    CHECK (estado IN ('BORRADOR','ACTIVO'));
ALTER TABLE caso       ADD CONSTRAINT ck_caso_asignacion_equipos
    CHECK (asignacion_equipos IN ('MANUAL','AUTOMATICA'));
ALTER TABLE empresa    ADD CONSTRAINT ck_empresa_estado
    CHECK (estado IN ('ACTIVA','INACTIVA','CERRADA'));
ALTER TABLE integrante ADD CONSTRAINT ck_integrante_departamento
    CHECK (departamento IN ('GERENCIA_GENERAL','COMERCIAL','OPERACIONES','ADMINISTRATIVA'));

-- 6. Verificacion: deben quedar solo las 12 tablas del modelo actual ---
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
ORDER BY table_name;

ROLLBACK;   -- cambiar por COMMIT cuando el resultado este bien

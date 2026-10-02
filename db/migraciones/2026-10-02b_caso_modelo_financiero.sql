-- Caso: modelo financiero completo + impacto de las opciones (PostgreSQL / Neon)
-- Requiere haber ejecutado 2026-10-02_caso_estado_resultados.sql (ventas_netas, costo_ventas, gastos_operativos).
-- Ejecutar ANTES de arrancar el backend con este código (ddl-auto=validate).
--
-- Las 19 partidas admiten NULL solo por los casos que ya existen; la API las exige en casos nuevos o editados.
-- activo_total, pasivo_total, patrimonio y utilidad_neta se mantienen: ahora los calcula el backend.

BEGIN;

-- 1. gastos_operativos se reemplaza por gastos_administracion + gastos_ventas
ALTER TABLE caso DROP CONSTRAINT IF EXISTS ck_caso_estado_resultados_no_negativos;
ALTER TABLE caso DROP COLUMN IF EXISTS gastos_operativos;

-- 2. Partidas nuevas (ventas_netas y costo_ventas ya existen)
ALTER TABLE caso
    -- Balance - Activo
    ADD COLUMN IF NOT EXISTS efectivo                              NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS cuentas_por_cobrar                    NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS inventarios                           NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS propiedad_planta_equipo               NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS activos_intangibles                   NUMERIC(18,2),
    -- Balance - Pasivo
    ADD COLUMN IF NOT EXISTS cuentas_por_pagar                     NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS obligaciones_financieras_corto_plazo  NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS obligaciones_financieras_largo_plazo  NUMERIC(18,2),
    -- Balance - Patrimonio
    ADD COLUMN IF NOT EXISTS capital_social                        NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS utilidades_retenidas                  NUMERIC(18,2),
    -- Estado de Resultados
    ADD COLUMN IF NOT EXISTS ventas_netas                          NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS costo_ventas                          NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS gastos_administracion                 NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS gastos_ventas                         NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS gastos_financieros                    NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS impuesto_renta                        NUMERIC(18,2),
    -- Flujo de Efectivo
    ADD COLUMN IF NOT EXISTS flujo_operativo                       NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS flujo_inversion                       NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS flujo_financiacion                    NUMERIC(18,2);

-- 3. No negativas (salvo utilidades retenidas y los 3 flujos, que pueden serlo)
ALTER TABLE caso DROP CONSTRAINT IF EXISTS ck_caso_partidas_no_negativas;
ALTER TABLE caso ADD CONSTRAINT ck_caso_partidas_no_negativas CHECK (
        COALESCE(efectivo, 0) >= 0
    AND COALESCE(cuentas_por_cobrar, 0) >= 0
    AND COALESCE(inventarios, 0) >= 0
    AND COALESCE(propiedad_planta_equipo, 0) >= 0
    AND COALESCE(activos_intangibles, 0) >= 0
    AND COALESCE(cuentas_por_pagar, 0) >= 0
    AND COALESCE(obligaciones_financieras_corto_plazo, 0) >= 0
    AND COALESCE(obligaciones_financieras_largo_plazo, 0) >= 0
    AND COALESCE(capital_social, 0) >= 0
    AND COALESCE(ventas_netas, 0) >= 0
    AND COALESCE(costo_ventas, 0) >= 0
    AND COALESCE(gastos_administracion, 0) >= 0
    AND COALESCE(gastos_ventas, 0) >= 0
    AND COALESCE(gastos_financieros, 0) >= 0
    AND COALESCE(impuesto_renta, 0) >= 0
);

-- 4. Impacto financiero de cada opción (JSON con hasta 6 rubros; NULL = sin impacto)
ALTER TABLE caso_opcion ADD COLUMN IF NOT EXISTS impacto TEXT;

COMMIT;

-- Verificación:
-- SELECT column_name FROM information_schema.columns
-- WHERE table_name = 'caso' ORDER BY ordinal_position;
-- SELECT column_name FROM information_schema.columns
-- WHERE table_name = 'caso_opcion' AND column_name = 'impacto';

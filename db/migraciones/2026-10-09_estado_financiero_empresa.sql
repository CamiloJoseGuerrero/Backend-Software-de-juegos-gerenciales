-- Estado financiero por empresa y caso: arrastre del balance entre casos (D2) (PostgreSQL / Neon)
-- Ejecutar ANTES de arrancar el backend con este código (ddl-auto=validate): sin esta tabla la app no inicia.
-- Requiere 2026-10-02b_caso_modelo_financiero.sql (mismas 19 partidas que la tabla caso).
--
-- Cada empresa tiene, por cada caso de su simulación, hasta dos filas:
--   INICIAL: balance con el que empieza el caso + estado de resultados del escenario del caso.
--            origen DOCENTE  = balance que definió el docente en el primer caso (también para
--                              empresas que entran a mitad de la simulación).
--            origen HEREDADO = balance FINAL de la empresa en el caso anterior.
--   FINAL:   resultado del caso después de aplicar la decisión.
--            origen DECISION     = se aplicó el impacto de la opción elegida (id_opcion).
--            origen PENALIZACION = la empresa no decidió; se aplicó la penalización del caso.
--
-- Reglas de cierre (aprobadas 2026-10-09):
--   utilidad neta del caso -> suma a utilidades_retenidas y a efectivo (el balance siempre cuadra);
--   el resto del balance pasa igual; flujo_operativo = utilidad neta; inversión y financiación = 0.
-- Por eso efectivo y utilidades_retenidas pueden quedar negativos (pérdidas mayores a la caja).
-- Los totales (activo, pasivo, patrimonio, utilidad neta) no se guardan: los calcula el backend.

BEGIN;

CREATE TABLE IF NOT EXISTS estado_financiero_empresa (
    id_estado_financiero_empresa          BIGSERIAL     PRIMARY KEY,
    id_empresa                            BIGINT        NOT NULL,
    id_caso                               BIGINT        NOT NULL,
    momento                               VARCHAR(10)   NOT NULL,
    origen                                VARCHAR(20)   NOT NULL,
    id_opcion                             BIGINT,
    calculado_en                          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Balance - Activo
    efectivo                              NUMERIC(18,2) NOT NULL,
    cuentas_por_cobrar                    NUMERIC(18,2) NOT NULL,
    inventarios                           NUMERIC(18,2) NOT NULL,
    propiedad_planta_equipo               NUMERIC(18,2) NOT NULL,
    activos_intangibles                   NUMERIC(18,2) NOT NULL,
    -- Balance - Pasivo
    cuentas_por_pagar                     NUMERIC(18,2) NOT NULL,
    obligaciones_financieras_corto_plazo  NUMERIC(18,2) NOT NULL,
    obligaciones_financieras_largo_plazo  NUMERIC(18,2) NOT NULL,
    -- Balance - Patrimonio
    capital_social                        NUMERIC(18,2) NOT NULL,
    utilidades_retenidas                  NUMERIC(18,2) NOT NULL,
    -- Estado de Resultados
    ventas_netas                          NUMERIC(18,2) NOT NULL,
    costo_ventas                          NUMERIC(18,2) NOT NULL,
    gastos_administracion                 NUMERIC(18,2) NOT NULL,
    gastos_ventas                         NUMERIC(18,2) NOT NULL,
    gastos_financieros                    NUMERIC(18,2) NOT NULL,
    impuesto_renta                        NUMERIC(18,2) NOT NULL,
    -- Flujo de Efectivo
    flujo_operativo                       NUMERIC(18,2) NOT NULL,
    flujo_inversion                       NUMERIC(18,2) NOT NULL,
    flujo_financiacion                    NUMERIC(18,2) NOT NULL,

    CONSTRAINT fk_efe_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE CASCADE,
    CONSTRAINT fk_efe_caso    FOREIGN KEY (id_caso)    REFERENCES caso(id_caso)       ON DELETE CASCADE,
    CONSTRAINT fk_efe_opcion  FOREIGN KEY (id_opcion)  REFERENCES caso_opcion(id_opcion) ON DELETE SET NULL,
    CONSTRAINT uq_efe_empresa_caso_momento UNIQUE (id_empresa, id_caso, momento),
    CONSTRAINT ck_efe_momento CHECK (momento IN ('INICIAL', 'FINAL')),
    CONSTRAINT ck_efe_origen CHECK (
           (momento = 'INICIAL' AND origen IN ('DOCENTE', 'HEREDADO'))
        OR (momento = 'FINAL'   AND origen IN ('DECISION', 'PENALIZACION'))
    ),
    CONSTRAINT ck_efe_opcion CHECK ((origen = 'DECISION') = (id_opcion IS NOT NULL)),
    -- No negativas, salvo efectivo, utilidades retenidas y los 3 flujos
    CONSTRAINT ck_efe_partidas_no_negativas CHECK (
            cuentas_por_cobrar >= 0
        AND inventarios >= 0
        AND propiedad_planta_equipo >= 0
        AND activos_intangibles >= 0
        AND cuentas_por_pagar >= 0
        AND obligaciones_financieras_corto_plazo >= 0
        AND obligaciones_financieras_largo_plazo >= 0
        AND capital_social >= 0
        AND ventas_netas >= 0
        AND costo_ventas >= 0
        AND gastos_administracion >= 0
        AND gastos_ventas >= 0
        AND gastos_financieros >= 0
        AND impuesto_renta >= 0
    )
);

CREATE INDEX IF NOT EXISTS ix_efe_caso ON estado_financiero_empresa (id_caso);

COMMIT;

-- Verificación:
-- SELECT column_name, data_type, is_nullable FROM information_schema.columns
-- WHERE table_name = 'estado_financiero_empresa' ORDER BY ordinal_position;
-- SELECT conname FROM pg_constraint WHERE conrelid = 'estado_financiero_empresa'::regclass;

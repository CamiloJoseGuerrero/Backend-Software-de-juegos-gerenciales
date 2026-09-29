-- Fase 2: Casos de una simulación (PostgreSQL / Neon)
-- Ejecutar ANTES de arrancar el backend con el código de Caso (ddl-auto=validate).
-- Modelo: una simulación tiene varios casos; cada caso tiene sus 3 fechas y una lista de opciones.

BEGIN;

CREATE TABLE IF NOT EXISTS caso (
    id_caso               BIGSERIAL PRIMARY KEY,
    id_simulacion         BIGINT        NOT NULL,
    -- Información general
    nombre_empresa        VARCHAR(150)  NOT NULL,
    mision                TEXT,
    vision                TEXT,
    tipo                  VARCHAR(100),
    -- Información financiera inicial (período 0)
    activo_total          NUMERIC(18,2) NOT NULL,
    pasivo_total          NUMERIC(18,2) NOT NULL,
    patrimonio            NUMERIC(18,2) NOT NULL,
    utilidad_neta         NUMERIC(18,2) NOT NULL,
    -- Penalización por no decidir, en %
    penalizacion_min      NUMERIC(5,2)  NOT NULL,
    penalizacion_max      NUMERIC(5,2)  NOT NULL,
    -- Fechas
    fecha_visualizacion   TIMESTAMP     NOT NULL,
    fecha_inicio          TIMESTAMP     NOT NULL,
    fecha_fin             TIMESTAMP     NOT NULL,

    CONSTRAINT fk_caso_simulacion FOREIGN KEY (id_simulacion)
        REFERENCES simulacion(id_simulacion) ON DELETE CASCADE,
    CONSTRAINT ck_caso_penalizacion
        CHECK (penalizacion_min >= 0 AND penalizacion_max <= 100 AND penalizacion_min <= penalizacion_max),
    CONSTRAINT ck_caso_fechas
        CHECK (fecha_visualizacion <= fecha_inicio AND fecha_inicio < fecha_fin)
);

CREATE INDEX IF NOT EXISTS idx_caso_simulacion ON caso (id_simulacion);

CREATE TABLE IF NOT EXISTS caso_opcion (
    id_opcion   BIGSERIAL PRIMARY KEY,
    id_caso     BIGINT  NOT NULL,
    orden       INTEGER NOT NULL,
    opcion      TEXT    NOT NULL,
    resultado   TEXT    NOT NULL,

    CONSTRAINT fk_opcion_caso FOREIGN KEY (id_caso)
        REFERENCES caso(id_caso) ON DELETE CASCADE,
    CONSTRAINT uq_opcion_caso_orden UNIQUE (id_caso, orden)
);

COMMIT;

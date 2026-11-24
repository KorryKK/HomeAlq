-- =====================================================
-- HOMEALQ
-- V1 - CREACIÓN DEL ESQUEMA INICIAL
-- SQLite
-- =====================================================
-- Diferencias con la versión de MySQL:
--   * Los ids son INTEGER PRIMARY KEY AUTOINCREMENT.
--   * SQLite no tiene tipo fecha: las fechas son texto yyyy-MM-dd
--     (la app siempre las escribe así) y se comparan como texto.
--   * SQLite no controla la longitud de los textos: la valida la app.
--   * Los índices se crean aparte, con CREATE INDEX.
--   * Las claves foráneas solo funcionan si la conexión las activa
--     (la URL lleva foreign_keys=on).


-- =====================================================
-- 1. HABITACION
-- =====================================================

CREATE TABLE habitacion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero INTEGER NOT NULL,

    CONSTRAINT uq_habitacion_numero
        UNIQUE (numero)
);


-- =====================================================
-- 2. PERSONA
-- =====================================================

CREATE TABLE persona (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    apellidos TEXT NOT NULL,
    dni TEXT NOT NULL,
    telefono TEXT NOT NULL,

    CONSTRAINT uq_persona_dni
        UNIQUE (dni)
);


-- =====================================================
-- 3. CONTRATO
-- =====================================================

CREATE TABLE contrato (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    habitacion_id INTEGER NOT NULL,
    tipo TEXT NOT NULL,
    fecha_inicio TEXT NOT NULL,
    fecha_fin TEXT,
    precio_mensual INTEGER NOT NULL,

    CONSTRAINT fk_contrato_habitacion
        FOREIGN KEY (habitacion_id)
        REFERENCES habitacion(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_contrato_tipo
        CHECK (tipo IN ('INDIVIDUAL', 'PAREJA')),

    CONSTRAINT chk_contrato_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin > fecha_inicio),

    CONSTRAINT chk_contrato_precio
        CHECK (precio_mensual > 0)
);

CREATE INDEX idx_contrato_habitacion ON contrato (habitacion_id);


-- =====================================================
-- 4. CONTRATO_PERSONA
-- =====================================================

CREATE TABLE contrato_persona (
    contrato_id INTEGER NOT NULL,
    persona_id INTEGER NOT NULL,

    PRIMARY KEY (contrato_id, persona_id),

    CONSTRAINT fk_contrato_persona_contrato
        FOREIGN KEY (contrato_id)
        REFERENCES contrato(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_contrato_persona_persona
        FOREIGN KEY (persona_id)
        REFERENCES persona(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE INDEX idx_contrato_persona_persona ON contrato_persona (persona_id);


-- =====================================================
-- 5. DOCUMENTO_CONTRATO
-- =====================================================

CREATE TABLE documento_contrato (
    contrato_id INTEGER NOT NULL PRIMARY KEY,
    nombre_archivo TEXT NOT NULL,
    contenido BLOB NOT NULL,
    fecha_subida TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_documento_contrato
        FOREIGN KEY (contrato_id)
        REFERENCES contrato(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


-- =====================================================
-- 6. FACTURA
-- =====================================================

CREATE TABLE factura (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    concepto TEXT NOT NULL,
    fecha_inicio TEXT NOT NULL,
    fecha_fin TEXT NOT NULL,
    importe_total INTEGER NOT NULL,
    fecha_registro TEXT NOT NULL,
    fecha_limite TEXT NOT NULL,
    penalizacion_por_dia INTEGER NOT NULL,

    CONSTRAINT chk_factura_fechas
        CHECK (fecha_fin >= fecha_inicio),

    CONSTRAINT chk_factura_importe
        CHECK (importe_total > 0),

    CONSTRAINT chk_factura_penalizacion
        CHECK (penalizacion_por_dia >= 0)
);

CREATE INDEX idx_factura_fecha ON factura (fecha_registro);


-- =====================================================
-- 7. REPARTO
-- =====================================================

CREATE TABLE reparto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    factura_id INTEGER NOT NULL,
    contrato_id INTEGER NOT NULL,
    dias INTEGER NOT NULL,
    personas INTEGER NOT NULL,
    importe INTEGER NOT NULL,
    estado TEXT NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago TEXT,
    dias_retraso INTEGER,
    penalizacion_cobrada INTEGER,

    CONSTRAINT uq_reparto_factura_contrato
        UNIQUE (factura_id, contrato_id),

    CONSTRAINT fk_reparto_factura
        FOREIGN KEY (factura_id)
        REFERENCES factura(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_reparto_contrato
        FOREIGN KEY (contrato_id)
        REFERENCES contrato(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_reparto_personas
        CHECK (personas IN (1, 2)),

    CONSTRAINT chk_reparto_dias
        CHECK (dias > 0),

    CONSTRAINT chk_reparto_importe
        CHECK (importe >= 0),

    CONSTRAINT chk_reparto_estado
        CHECK (estado IN ('PENDIENTE', 'PAGADO')),

    CONSTRAINT chk_reparto_retraso
        CHECK (dias_retraso IS NULL OR dias_retraso >= 0),

    CONSTRAINT chk_reparto_penalizacion
        CHECK (penalizacion_cobrada IS NULL OR penalizacion_cobrada >= 0),

    CONSTRAINT chk_reparto_pago
        CHECK (
            (estado = 'PENDIENTE'
                AND fecha_pago IS NULL
                AND dias_retraso IS NULL
                AND penalizacion_cobrada IS NULL)
            OR
            (estado = 'PAGADO'
                AND fecha_pago IS NOT NULL
                AND dias_retraso IS NOT NULL
                AND penalizacion_cobrada IS NOT NULL)
        )
);

CREATE INDEX idx_reparto_contrato ON reparto (contrato_id);


-- =====================================================
-- 8. COBRO_ALQUILER
-- =====================================================

CREATE TABLE cobro_alquiler (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    contrato_id INTEGER NOT NULL,
    mes TEXT NOT NULL,
    importe INTEGER NOT NULL,
    fecha_desde TEXT NOT NULL,
    fecha_limite TEXT NOT NULL,
    penalizacion_por_dia INTEGER NOT NULL,
    estado TEXT NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago TEXT,
    dias_retraso INTEGER,
    penalizacion_cobrada INTEGER,

    CONSTRAINT uq_cobro_contrato_mes
        UNIQUE (contrato_id, mes),

    CONSTRAINT fk_cobro_alquiler_contrato
        FOREIGN KEY (contrato_id)
        REFERENCES contrato(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    -- mes es siempre el día 1 (yyyy-MM-01)
    CONSTRAINT chk_cobro_mes
        CHECK (
            length(mes) = 10
            AND date(mes) IS NOT NULL
            AND strftime('%d', mes) = '01'
        ),

    CONSTRAINT chk_cobro_importe
        CHECK (importe > 0),

    CONSTRAINT chk_cobro_fechas
        CHECK (fecha_limite >= fecha_desde),

    CONSTRAINT chk_cobro_penalizacion_diaria
        CHECK (penalizacion_por_dia >= 0),

    CONSTRAINT chk_cobro_estado
        CHECK (estado IN ('PENDIENTE', 'PAGADO')),

    CONSTRAINT chk_cobro_retraso
        CHECK (dias_retraso IS NULL OR dias_retraso >= 0),

    CONSTRAINT chk_cobro_penalizacion
        CHECK (penalizacion_cobrada IS NULL OR penalizacion_cobrada >= 0),

    CONSTRAINT chk_cobro_pago
        CHECK (
            (estado = 'PENDIENTE'
                AND fecha_pago IS NULL
                AND dias_retraso IS NULL
                AND penalizacion_cobrada IS NULL)
            OR
            (estado = 'PAGADO'
                AND fecha_pago IS NOT NULL
                AND dias_retraso IS NOT NULL
                AND penalizacion_cobrada IS NOT NULL)
        )
);

CREATE INDEX idx_cobro_mes ON cobro_alquiler (mes);


-- =====================================================
-- 9. CONFIGURACIÓN
-- =====================================================

CREATE TABLE configuracion (
    id INTEGER NOT NULL PRIMARY KEY,
    alquiler_dia_desde INTEGER NOT NULL,
    alquiler_dia_hasta INTEGER NOT NULL,
    alquiler_penalizacion_dia INTEGER NOT NULL,
    factura_dias_plazo INTEGER NOT NULL,
    factura_penalizacion_dia INTEGER NOT NULL,

    CONSTRAINT chk_configuracion_id
        CHECK (id = 1),

    CONSTRAINT chk_configuracion_dias
        CHECK (
            alquiler_dia_desde BETWEEN 1 AND 31
            AND alquiler_dia_hasta BETWEEN 1 AND 31
        ),

    CONSTRAINT chk_configuracion_penalizaciones
        CHECK (
            alquiler_penalizacion_dia >= 0
            AND factura_penalizacion_dia >= 0
        ),

    CONSTRAINT chk_configuracion_plazo
        CHECK (factura_dias_plazo >= 0)
);


-- =====================================================
-- 10. USUARIO
-- =====================================================

CREATE TABLE usuario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_usuario TEXT NOT NULL,
    contrasena_hash TEXT NOT NULL,

    CONSTRAINT uq_usuario_nombre
        UNIQUE (nombre_usuario)
);

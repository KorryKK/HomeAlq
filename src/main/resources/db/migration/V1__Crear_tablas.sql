
-- =====================================================
-- HOMEALQ
-- V1 - CREACIÓN DEL ESQUEMA INICIAL
-- MySQL 8.4.8
-- =====================================================


-- =====================================================
-- 1. HABITACION
-- =====================================================

CREATE TABLE habitacion (
    id INT AUTO_INCREMENT PRIMARY KEY
    ,
    numero INT NOT NULL,

    CONSTRAINT uq_habitacion_numero
        UNIQUE (numero)
) ENGINE=InnoDB;


-- =====================================================
-- 2. PERSONA
-- =====================================================

CREATE TABLE persona (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(40) NOT NULL,
    apellidos VARCHAR(60) NOT NULL,
    dni VARCHAR(20) NOT NULL,
    telefono VARCHAR(20),

    CONSTRAINT uq_persona_dni
        UNIQUE (dni)
) ENGINE=InnoDB;


-- =====================================================
-- 3. CONTRATO
-- =====================================================

CREATE TABLE contrato (
    id INT AUTO_INCREMENT PRIMARY KEY,
    habitacion_id INT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    precio_mensual INT NOT NULL,

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
        CHECK (precio_mensual > 0),

    INDEX idx_contrato_habitacion (habitacion_id)
) ENGINE=InnoDB;


-- =====================================================
-- 4. CONTRATO_PERSONA
-- =====================================================

CREATE TABLE contrato_persona (
    contrato_id INT NOT NULL,
    persona_id INT NOT NULL,

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
        ON UPDATE CASCADE,

    INDEX idx_contrato_persona_persona (persona_id)
) ENGINE=InnoDB;


-- =====================================================
-- 5. DOCUMENTO_CONTRATO
-- =====================================================

CREATE TABLE documento_contrato (
    contrato_id INT NOT NULL PRIMARY KEY,
    nombre_archivo VARCHAR(255) NOT NULL,
    contenido MEDIUMBLOB NOT NULL,
    fecha_subida DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_documento_contrato
        FOREIGN KEY (contrato_id)
        REFERENCES contrato(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;


-- =====================================================
-- 6. FACTURA
-- =====================================================

CREATE TABLE factura (
    id INT AUTO_INCREMENT PRIMARY KEY,
    concepto VARCHAR(100) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    importe_total INT NOT NULL,
    fecha_registro DATE NOT NULL,
    fecha_limite DATE NOT NULL,
    penalizacion_por_dia INT NOT NULL,

    CONSTRAINT chk_factura_fechas
        CHECK (fecha_fin >= fecha_inicio),

    CONSTRAINT chk_factura_importe
        CHECK (importe_total > 0),

    CONSTRAINT chk_factura_penalizacion
        CHECK (penalizacion_por_dia >= 0),

    INDEX idx_factura_fecha (fecha_registro)
) ENGINE=InnoDB;


-- =====================================================
-- 7. REPARTO
-- =====================================================

CREATE TABLE reparto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    factura_id INT NOT NULL,
    contrato_id INT NOT NULL,
    dias INT NOT NULL,
    personas INT NOT NULL,
    importe INT NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago DATE,
    dias_retraso INT,
    penalizacion_cobrada INT,

    CONSTRAINT uq_reparto_factura_contrato
        UNIQUE (factura_id, contrato_id),

    CONSTRAINT fk_reparto_factura
        FOREIGN KEY (factura_id)
        REFERENCES factura(id)
        ON DELETE CASCADE
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
        ),

    INDEX idx_reparto_contrato (contrato_id)
) ENGINE=InnoDB;


-- =====================================================
-- 8. COBRO_ALQUILER
-- =====================================================

CREATE TABLE cobro_alquiler (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contrato_id INT NOT NULL,
    mes DATE NOT NULL,
    importe INT NOT NULL,
    fecha_desde DATE NOT NULL,
    fecha_limite DATE NOT NULL,
    penalizacion_por_dia INT NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago DATE,
    dias_retraso INT,
    penalizacion_cobrada INT,

    CONSTRAINT uq_cobro_contrato_mes
        UNIQUE (contrato_id, mes),

    CONSTRAINT fk_cobro_alquiler_contrato
        FOREIGN KEY (contrato_id)
        REFERENCES contrato(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_cobro_mes
        CHECK (DAY(mes) = 1),

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
        ),

    INDEX idx_cobro_mes (mes)
) ENGINE=InnoDB;


-- =====================================================
-- 9. CONFIGURACIÓN
-- =====================================================

CREATE TABLE configuracion (
    id INT NOT NULL PRIMARY KEY,
    alquiler_dia_desde INT NOT NULL,
    alquiler_dia_hasta INT NOT NULL,
    alquiler_penalizacion_dia INT NOT NULL,
    factura_dias_plazo INT NOT NULL,
    factura_penalizacion_dia INT NOT NULL,

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
) ENGINE=InnoDB;


-- =====================================================
-- 10. USUARIO
-- =====================================================

CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) NOT NULL,
    contrasena_hash VARCHAR(255) NOT NULL,

    CONSTRAINT uq_usuario_nombre
        UNIQUE (nombre_usuario)
) ENGINE=InnoDB;

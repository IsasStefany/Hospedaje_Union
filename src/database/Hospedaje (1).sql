-- ============================================================================
-- SISTEMA DE CONTROL HOTELERO - "HOSPEDAJE UNION"
-- ============================================================================

-- RECREACIÓN DE LA BASE DE DATOS (MANTENIMIENTO LIMPIO)
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'HospedajeUnion')
BEGIN
    ALTER DATABASE HospedajeUnion SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE HospedajeUnion;
END
GO

CREATE DATABASE HospedajeUnion;
GO

USE HospedajeUnion;
GO

-- ============================================================================
-- SECTION 1: ESTRUCTURA DE TABLAS (MODELO RELACIONAL MAESTRO)
-- ============================================================================

-- TABLA 1: USUARIOS (Personal de recepción y administración)
CREATE TABLE usuarios (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    nombre          VARCHAR(100)    NOT NULL,
    usuario         VARCHAR(50)     NOT NULL UNIQUE,
    contrasena      VARCHAR(255)    NOT NULL,
    rol             VARCHAR(20)     NOT NULL DEFAULT 'OPERADOR', -- ADMIN | OPERADOR
    activo          BIT             NOT NULL DEFAULT 1,
    fecha_creacion  DATETIME        NOT NULL DEFAULT GETDATE()
);
GO

-- TABLA 2: HABITACIONES (Inventario integrado con tarifas dinámicas)
CREATE TABLE habitaciones (
    id                 INT IDENTITY(1,1) PRIMARY KEY,
    numero             VARCHAR(10)     NOT NULL UNIQUE,
    piso               INT             NOT NULL,
    tipo               VARCHAR(50)     NOT NULL,
    descripcion        VARCHAR(200),
    precio_noche       DECIMAL(8,2)    NOT NULL,
    precio_cama1       DECIMAL(8,2)    NULL, -- Para cuartos dobles operados como simples
    precio_rebaja      DECIMAL(8,2)    NULL, -- [AGREGADO] Tarifa promocional por noche
    dias_minimo_rebaja INT             NOT NULL DEFAULT 0, -- [AGREGADO] Candado de días para descuento
    es_cuarto_especial BIT             NOT NULL DEFAULT 0, -- [AGREGADO] Indicador premium (0 o 1)
    estado             VARCHAR(20)     NOT NULL DEFAULT 'DISPONIBLE', -- DISPONIBLE | OCUPADO | MANTENIMIENTO
    tiene_banio_propio BIT             NOT NULL DEFAULT 0,
    CONSTRAINT chk_estado_hab CHECK (estado IN ('DISPONIBLE','OCUPADO','MANTENIMIENTO'))
);
GO

-- TABLA 3: HUÉSPEDES (Registro y maestro de clientes)
CREATE TABLE huespedes (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    nombres         VARCHAR(100)    NOT NULL,
    apellidos       VARCHAR(100)    NOT NULL,
    dni             VARCHAR(15)     NOT NULL,
    procedencia     VARCHAR(100),
    motivo_visita   VARCHAR(100),
    telefono        VARCHAR(20),
    fecha_registro  DATETIME        NOT NULL DEFAULT GETDATE()
);
GO

-- TABLA 4: RESERVAS (Transacciones operativas de alquiler de cuartos)
CREATE TABLE reservas (
    id                  INT IDENTITY(1,1) PRIMARY KEY,
    huesped_id          INT             NOT NULL REFERENCES huespedes(id),
    habitacion_id       INT             NOT NULL REFERENCES habitaciones(id),
    usuario_id          INT             NOT NULL REFERENCES usuarios(id),
    fecha_entrada       DATETIME        NOT NULL,
    fecha_vencimiento   DATETIME        NOT NULL, -- Límite estándar (12:00 PM del día siguiente)
    fecha_salida_real   DATETIME        NULL,
    precio_cobrado      DECIMAL(8,2)    NOT NULL,
    uso_camas           INT             NOT NULL DEFAULT 2, -- 1 o 2 camas según tipo
    estado              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVA', -- ACTIVA | FINALIZADA | EXTENDIDA
    observaciones       VARCHAR(500),
    fecha_creacion      DATETIME        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT chk_estado_res CHECK (estado IN ('ACTIVA','FINALIZADA','EXTENDIDA'))
);
GO

-- TABLA 5: BOLETAS (Comprobantes financieros de venta)
CREATE TABLE boletas (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    numero_boleta   VARCHAR(20)     NOT NULL UNIQUE,
    reserva_id      INT             NOT NULL REFERENCES reservas(id),
    fecha_emision   DATETIME        NOT NULL DEFAULT GETDATE(),
    monto_total     DECIMAL(8,2)    NOT NULL,
    ruta_pdf        VARCHAR(500)    NULL, -- [AGREGADO] Auditoría física del archivo en disco
    usuario_id      INT             NOT NULL REFERENCES usuarios(id) -- [AGREGADO] Trazabilidad del emisor
);
GO

-- TABLA 6: CAJA DIARIA (Cierres contables diarios de auditoría)
CREATE TABLE caja_diaria (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    fecha           DATE            NOT NULL UNIQUE,
    total_ingresos  DECIMAL(10,2)   NOT NULL DEFAULT 0,
    total_gastos    DECIMAL(10,2)   NOT NULL DEFAULT 0,
    saldo_neto      DECIMAL(10,2)   NOT NULL DEFAULT 0,
    cerrado         BIT             NOT NULL DEFAULT 0,
    usuario_cierre  INT             NULL REFERENCES usuarios(id),
    hora_cierre     DATETIME        NULL,
    observaciones   VARCHAR(500)
);
GO

-- TABLA 7: GASTOS (Registro complementario de egresos y caja chica)
CREATE TABLE gastos (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    fecha           DATE            NOT NULL,
    concepto        VARCHAR(200)    NOT NULL,
    monto           DECIMAL(8,2)    NOT NULL,
    usuario_id      INT             NOT NULL REFERENCES usuarios(id),
    fecha_registro  DATETIME        NOT NULL DEFAULT GETDATE()
);
GO


-- TABLA 8: Método de pago a reservas
ALTER TABLE reservas ADD metodo_pago VARCHAR(20) NOT NULL DEFAULT 'EFECTIVO';
-- EFECTIVO | YAPE | VISA | BCP
GO
 

-- TABLA 9: Servicios de lavandería
DROP TABLE lavanderia;
GO

CREATE TABLE lavanderia (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    reserva_id      INT             NOT NULL REFERENCES reservas(id),
    habitacion_id   INT             NOT NULL REFERENCES habitaciones(id),
    nombre_huesped  VARCHAR(200)    NOT NULL,
    kilos           DECIMAL(6,2)    NOT NULL,
    costo_total     DECIMAL(8,2)    NOT NULL,
    tipo            VARCHAR(20)     NOT NULL,
    estado          VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE',
    observaciones   VARCHAR(500),
    usuario_id      INT             NOT NULL REFERENCES usuarios(id),
    fecha_registro  DATETIME        NOT NULL DEFAULT GETDATE(),
    fecha_entrega   DATETIME        NULL,
    CONSTRAINT chk_tipo_lav   CHECK (tipo   IN ('ROPA_HUESPED','SABANAS_TOALLAS','MIXTO')),
    CONSTRAINT chk_estado_lav CHECK (estado IN ('PENDIENTE','EN_PROCESO','LISTO','ENTREGADO'))
);
GO
-- Vista lavanderia con datos de habitacion y huesped
CREATE VIEW v_lavanderia AS
SELECT
    l.id,
    l.reserva_id,
    h.numero        AS habitacion,
    l.nombre_huesped,
    l.kilos,
    l.costo_total,
    l.tipo,
    l.estado,
    l.observaciones,
    l.fecha_registro,
    l.fecha_entrega,
    u.nombre        AS registrado_por
FROM lavanderia l
JOIN habitaciones h ON h.id = l.habitacion_id
JOIN usuarios     u ON u.id = l.usuario_id;
GO

ALTER TABLE lavanderia 
ADD fecha_entrega_estimada DATETIME NULL;
GO
 
-- Calcular fecha estimada para registros existentes
-- Regla: fecha_registro + kilos horas, max 20:00, si pasa -> dia siguiente 08:00
UPDATE lavanderia
SET fecha_entrega_estimada = 
    CASE 
        WHEN DATEADD(HOUR, CEILING(kilos), fecha_registro) >= 
             DATEADD(HOUR, 20, CAST(CAST(fecha_registro AS DATE) AS DATETIME))
        THEN DATEADD(HOUR, 8, 
             CAST(DATEADD(DAY, 1, CAST(fecha_registro AS DATE)) AS DATETIME))
        ELSE DATEADD(HOUR, CEILING(kilos), fecha_registro)
    END
WHERE fecha_entrega_estimada IS NULL;
GO
 
PRINT 'Columna fecha_entrega_estimada agregada correctamente.';
GO
 
PRINT 'Tablas de metodo_pago y lavanderia creadas correctamente.';
GO
 

 

-- ============================================================================
-- SECTION 2: COMPONENTES DE CONSULTA AVANZADA (VISTAS RELACIONALES)
-- ============================================================================

-- VISTA: Monitoreo integral en tiempo real del estado de ocupación del hotel
CREATE VIEW v_ocupacion_actual AS
SELECT
    h.numero,
    h.piso,
    h.tipo,
    h.precio_noche,
    h.estado,
    r.id                            AS reserva_id,
    hu.nombres + ' ' + hu.apellidos AS huesped,
    hu.dni,
    r.fecha_entrada,
    r.fecha_vencimiento,
    r.precio_cobrado
FROM habitaciones h
LEFT JOIN reservas r ON r.habitacion_id = h.id AND r.estado = 'ACTIVA'
LEFT JOIN huespedes hu ON hu.id = r.huesped_id;
GO


-- ============================================================================
-- SECTION 3: INSERCIÓN DE DATOS SEMILLA (REGISTROS INICIALES)
-- ============================================================================

-- INSERCIÓN: Cuenta administrativa por defecto (Contraseña en texto plano para pruebas locales)
INSERT INTO usuarios (nombre, usuario, contrasena, rol)
VALUES ('Administrador', 'admin', 'admin123', 'ADMIN');

-- INSERCIÓN: Catálogo maestro de inventario (22 Habitaciones)
-- Bloque A: Segundo piso (12 cuartos con baño propio o compartido)
INSERT INTO habitaciones (numero, piso, tipo, descripcion, precio_noche, tiene_banio_propio) VALUES
('201', 2, 'SIMPLE',      'Cama plaza y media, TV, silla, ventilador, baño compartido', 25.00, 0),
('202', 2, 'SIMPLE',      'Cama plaza y media, TV, silla, ventilador, baño compartido', 25.00, 0),
('203', 2, 'SIMPLE',      'Cama plaza y media, TV, silla, ventilador, baño compartido', 25.00, 0),
('204', 2, 'SIMPLE',      'Cama plaza y media, TV, silla, ventilador, baño compartido', 25.00, 0),
('205', 2, 'SIMPLE',      'Cama plaza y media, TV, silla, ventilador, baño compartido', 25.00, 0),
('206', 2, 'SIMPLE',      'Cama plaza y media, TV, silla, ventilador, baño compartido', 25.00, 0),
('207', 2, 'SIMPLE',      'Cama plaza y media, TV, silla, ventilador, baño compartido', 25.00, 0),
('208', 2, 'SIMPLE',      'Cama plaza y media, TV, silla, ventilador, baño compartido', 25.00, 0),
('209', 2, 'SIMPLE',      'Cama plaza y media, TV, silla, ventilador, baño compartido', 25.00, 0),
('210', 2, 'MATRIMONIAL', 'Cama 2 plazas, baño propio enmayolicado, TV pantalla plana, mesa, silla', 45.00, 1),
('211', 2, 'MATRIMONIAL', 'Cama 2 plazas, baño propio enmayolicado, TV pantalla plana, mesa, silla', 45.00, 1),
('212', 2, 'MATRIMONIAL', 'Cama 2 plazas, baño propio enmayolicado, TV pantalla plana, mesa, silla', 45.00, 1);

-- Bloque B: Tercer piso (10 cuartos incluyendo dobles con camas configurables)
INSERT INTO habitaciones (numero, piso, tipo, descripcion, precio_noche, precio_cama1, tiene_banio_propio) VALUES
('301', 3, 'SIMPLE',      'Cama plaza y media, TV, silla, ventilador, baño compartido', 25.00, NULL, 0),
('302', 3, 'SIMPLE',      'Cama plaza y media, TV, silla, ventilador, baño compartido', 25.00, NULL, 0),
('303', 3, 'SIMPLE',      'Cama plaza y media, TV, silla, ventilador, baño compartido', 25.00, NULL, 0),
('304', 3, 'SIMPLE_2P',   'Cama 2 plazas, lavadero enmayolicado, baño compartido', 25.00, NULL, 0),
('305', 3, 'SIMPLE_2P',   'Cama 2 plazas, lavadero enmayolicado, baño compartido', 25.00, NULL, 0),
('306', 3, 'DOBLE',       '2 camas plaza y media, baño propio enmayolicado, mesa, silla, ventilador', 45.00, 35.00, 1),
('307', 3, 'DOBLE',       '2 camas plaza y media, baño propio enmayolicado, mesa, silla, ventilador', 45.00, 35.00, 1),
('308', 3, 'DOBLE',       '2 camas plaza y media, baño propio enmayolicado, mesa, silla, ventilador', 45.00, 35.00, 1),
('309', 3, 'INDIVIDUAL',  'Cama plaza y media, baño propio enmayolicado, mesa, silla, ventilador', 35.00, NULL, 1),
('310', 3, 'INDIVIDUAL',  'Cama plaza y media, baño propio enmayolicado, mesa, silla, ventilador', 35.00, NULL, 1);
GO


-- ============================================================================
-- SECTION 4: SEGURIDAD PERIMETRAL Y ASIGNACIÓN DE PRIVILEGIOS DE LA APP
-- ============================================================================

USE HospedajeUnion;
GO

-- 1. Eliminación preventiva del login anterior para evitar duplicidad
IF EXISTS (SELECT name FROM sys.server_principals WHERE name = 'app_hotel')
BEGIN
    DROP LOGIN [app_hotel];
END
GO

-- 2. Creación del login maestro exclusivo para la persistencia del sistema de escritorio
CREATE LOGIN [app_hotel] WITH PASSWORD = 'UTP123', CHECK_POLICY = OFF;
GO

USE HospedajeUnion;
GO

-- 3. Mapeo del usuario dentro de la base de datos específica del hotel
CREATE USER [app_hotel] FOR LOGIN [app_hotel];
GO

-- 4. Asignación del rol con máximos privilegios de datos (Lectura y Escritura) dentro de la BD
ALTER ROLE db_owner ADD MEMBER [app_hotel];
GO

PRINT 'Base de datos HospedajeUnion y usuario app_hotel creados con exito a nivel experto.';
GO





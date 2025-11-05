-- Database Initialization Script for Transportista System

-- Create keycloak database
CREATE DATABASE keycloak_db;

-- Switch to transportista_db (this is already set as default in docker-compose)
-- The rest of the script runs in transportista_db

-- IMPORTANTE: No insertamos datos aquí porque las tablas aún no existen
-- Las tablas serán creadas automáticamente por Hibernate cuando los servicios inicien
-- Los datos se pueden insertar después manualmente o mediante la API

-- Este script solo crea las bases de datos necesarias
-- Los datos de prueba se insertarán después del primer inicio

-- Si quieres insertar datos automáticamente, descomenta este bloque
-- DESPUÉS de que los servicios hayan iniciado por primera vez:

/*
-- Sample Tarifas Data
INSERT INTO tarifas (tipo_tramo, costo_por_km, gestion_fija, consumo_combustible_por_km, precio_combustible_por_litro, tarifa_estadia_deposito_por_dia, fecha_creacion, fecha_actualizacion, activo) 
VALUES
('ORIGEN_DEPOSITO', 5.50, 1000.00, 0.35, 150.00, 500.00, NOW(), NOW(), true),
('DEPOSITO_DEPOSITO', 4.80, 800.00, 0.30, 150.00, 500.00, NOW(), NOW(), true),
('DEPOSITO_DESTINO', 5.50, 1000.00, 0.35, 150.00, 500.00, NOW(), NOW(), true),
('ORIGEN_DESTINO', 6.00, 1500.00, 0.40, 150.00, 0.00, NOW(), NOW(), true)
ON CONFLICT DO NOTHING;

-- Sample Clientes Data
INSERT INTO clientes (nombre, apellido, dni, domicilio, telefono, email, fecha_registro, activo) 
VALUES
('Juan', 'Pérez', '12345678', 'Av. Corrientes 1234, CABA', '+5491123456789', 'juan.perez@example.com', NOW(), true),
('María', 'González', '23456789', 'Calle Falsa 567, Rosario', '+5493414567890', 'maria.gonzalez@example.com', NOW(), true),
('Carlos', 'Rodríguez', '34567890', 'San Martín 890, Córdoba', '+5493514567891', 'carlos.rodriguez@example.com', NOW(), true)
ON CONFLICT (email) DO NOTHING;

-- Sample Depositos Data
INSERT INTO depositos (nombre, direccion, latitud, longitud, capacidad_maxima, capacidad_actual, fecha_creacion, activo) 
VALUES
('Depósito Norte', 'Panamericana Km 40, Buenos Aires', -34.5011, -58.5156, 100, 0, NOW(), true),
('Depósito Sur', 'Ruta 205 Km 15, La Plata', -34.9205, -57.9544, 80, 0, NOW(), true),
('Depósito Centro', 'Circunvalación Oeste, Rosario', -32.9468, -60.6393, 120, 0, NOW(), true),
('Depósito Córdoba', 'Ruta E53 Km 10, Córdoba', -31.4201, -64.1888, 90, 0, NOW(), true)
ON CONFLICT (nombre) DO NOTHING;

-- Sample Camiones Data
INSERT INTO camiones (patente, transportista, capacidad_peso, capacidad_volumen, costo_por_km, disponible, fecha_creacion, activo) 
VALUES
('AB123CD', 'Transporte López', 25.0, 60.0, 4.5, true, NOW(), true),
('EF456GH', 'Transportes del Norte', 30.0, 80.0, 5.0, true, NOW(), true),
('IJ789KL', 'Logística Sur', 20.0, 50.0, 4.0, true, NOW(), true),
('MN012OP', 'Cargo Express', 28.0, 70.0, 4.8, true, NOW(), true),
('QR345ST', 'Trans Continental', 32.0, 85.0, 5.5, true, NOW(), true)
ON CONFLICT (patente) DO NOTHING;
*/
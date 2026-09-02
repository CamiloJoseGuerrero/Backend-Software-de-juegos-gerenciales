-- Script de inicialización de usuarios para pruebas

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    numero_identificacion VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL
);

-- Usuario de prueba: docente
-- Contraseña: "docente123" (hasheada con BCrypt)
-- Hash: $2a$10$DlH.aBL6Vs4RXSdSs7m9jeLBE1I92r2WVMXlAKbVDwh/w9K8qvj1u
INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
VALUES ('Juan Perez', 'docente@email.com', '123456789', 
        '$2a$10$DlH.aBL6Vs4RXSdSs7m9jeLBE1I92r2WVMXlAKbVDwh/w9K8qvj1u', 'DOCENTE')
ON CONFLICT DO NOTHING;

-- Usuario de prueba: estudiante
-- Contraseña: "estudiante123" (hasheada con BCrypt)
-- Hash: $2a$10$Ll0zL.d8H7Qa3KVWqgKSuu5yrK6fVKT9KPSqSHMnHr.8mQ8zWwf6K
INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
VALUES ('Carlos Rodriguez', 'estudiante@email.com', '987654321', 
        '$2a$10$Ll0zL.d8H7Qa3KVWqgKSuu5yrK6fVKT9KPSqSHMnHr.8mQ8zWwf6K', 'ESTUDIANTE')
ON CONFLICT DO NOTHING;

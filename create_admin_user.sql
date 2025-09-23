-- =====================================================
-- Script para crear usuario administrador en MySQL
-- Base de datos: farmaciadb
-- =====================================================

-- Usar la base de datos farmaciadb
USE farmaciadb;

-- Verificar si la tabla usuarios existe, si no existe crearla
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'user'
);

-- Insertar usuario administrador
-- Nota: En un entorno real, la contraseña debería estar encriptada
-- IMPORTANTE: El rol debe estar en MAYÚSCULAS para que Spring Security lo reconozca
INSERT INTO usuarios (username, password, rol) 
VALUES ('admin', 'admin123', 'ADMIN')
ON DUPLICATE KEY UPDATE 
    password = 'admin123',
    rol = 'ADMIN';

-- Verificar que el usuario fue creado correctamente
SELECT * FROM usuarios WHERE rol = 'ADMIN';

-- =====================================================
-- Información del usuario creado:
-- Username: admin
-- Password: admin123
-- Rol: ADMIN
-- =====================================================

-- Opcional: Crear usuarios adicionales para testing
INSERT INTO usuarios (username, password, rol) 
VALUES 
    ('medico1', 'medico123', 'MEDICO'),
    ('farmaceutico1', 'farma123', 'FARMACEUTICO')
ON DUPLICATE KEY UPDATE 
    password = VALUES(password),
    rol = VALUES(rol);

-- Mostrar todos los usuarios creados
SELECT id, username, rol, 'Contraseña oculta por seguridad' as password_info 
FROM usuarios 
ORDER BY rol, username;
-- =====================================================
-- Script para crear usuario médico de prueba
-- =====================================================

USE farmaciadb;

-- Crear usuario médico de prueba
INSERT INTO usuarios (username, password, rol) 
VALUES ('medico', 'medico123', 'MEDICO')
ON DUPLICATE KEY UPDATE 
    password = 'medico123',
    rol = 'MEDICO';

-- Verificar usuarios existentes
SELECT 
    id,
    username,
    rol,
    CONCAT('ROLE_', rol) as spring_role
FROM usuarios 
ORDER BY rol, username;

-- =====================================================
-- Usuarios de prueba disponibles:
-- 
-- ADMIN:  admin / admin123
-- MEDICO: medico / medico123
-- =====================================================

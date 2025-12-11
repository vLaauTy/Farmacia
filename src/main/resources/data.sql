-- Script de inicialización de datos para H2 Database
-- Este script se ejecuta automáticamente al iniciar la aplicación si la base de datos está vacía

-- Insertar usuario administrador por defecto
-- Usuario: admin
-- Contraseña: admin (sin encriptación como está configurado en el sistema)
INSERT INTO usuarios (username, password, rol) 
SELECT 'admin', 'admin', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'admin');


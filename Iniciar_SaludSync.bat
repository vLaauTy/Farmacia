@echo off
title SaludSync - Sistema de Turnos Medicos
color 0A

echo.
echo ========================================
echo   SaludSync - Sistema de Turnos Medicos
echo ========================================
echo.
echo [INFO] Buscando el archivo ejecutable...
echo.

REM Cambiar al directorio donde está el script
cd /d "%~dp0"

REM Verificar si existe el JAR
if not exist "target\Farmacia-0.0.1-SNAPSHOT.jar" (
    color 0C
    echo [ERROR] No se encontro el archivo JAR compilado.
    echo.
    echo El archivo deberia estar en:
    echo %~dp0target\Farmacia-0.0.1-SNAPSHOT.jar
    echo.
    echo Debes compilar el proyecto primero:
    echo   1. Ejecuta: build.bat
    echo   2. O ejecuta: mvnw.cmd clean package
    echo.
    echo Presiona cualquier tecla para salir...
    pause >nul
    exit /b 1
)

echo [OK] Archivo JAR encontrado
echo.

REM Verificar si Java está instalado
java -version >nul 2>&1
if errorlevel 1 (
    color 0C
    echo [ERROR] Java no esta instalado o no esta en el PATH.
    echo.
    echo Descarga e instala Java 21 o superior desde:
    echo https://www.oracle.com/java/technologies/downloads/
    echo.
    echo Presiona cualquier tecla para salir...
    pause >nul
    exit /b 1
)

echo [OK] Java encontrado
echo.

REM Verificar si el puerto 8000 ya está en uso
netstat -ano | findstr ":8000" >nul
if not errorlevel 1 (
    color 0E
    echo [ADVERTENCIA] El puerto 8000 ya esta en uso.
    echo.
    echo Es posible que la aplicacion ya este corriendo.
    echo Verifica en tu navegador: http://localhost:8000
    echo.
    echo Si no funciona, cierra la aplicacion anterior y vuelve a ejecutar este script.
    echo.
    echo Presiona cualquier tecla para continuar de todos modos...
    pause >nul
    echo.
)

REM Ejecutar la aplicación
echo ========================================
echo   INICIANDO APLICACION...
echo ========================================
echo.
echo [ESPERA] Esto puede tardar 10-20 segundos...
echo.
echo Una vez que veas el mensaje:
echo "Started FarmaciaApplication in X seconds"
echo.
echo Podras acceder en tu navegador a:
echo.
echo   URL: http://localhost:8000
echo.
echo   Usuario: admin
echo   Password: admin
echo.
echo ========================================
echo   [!] NO CIERRES ESTA VENTANA
echo   [!] Presiona Ctrl+C para detener
echo ========================================
echo.
echo.

java -jar target\Farmacia-0.0.1-SNAPSHOT.jar

REM Si el programa termina con error
if errorlevel 1 (
    color 0C
    echo.
    echo.
    echo ========================================
    echo   [ERROR] La aplicacion termino con un error
    echo ========================================
    echo.
    echo Revisa los mensajes de error anteriores.
    echo.
    echo Problemas comunes:
    echo   - Puerto 8000 ya en uso
    echo   - Base de datos corrupta (elimina la carpeta 'data')
    echo   - Version incorrecta de Java
    echo.
)

echo.
echo Presiona cualquier tecla para cerrar esta ventana...
pause >nul

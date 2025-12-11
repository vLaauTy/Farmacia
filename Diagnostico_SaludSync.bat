@echo off
title SaludSync - Diagnostico
color 0B

echo.
echo ========================================
echo   SaludSync - Diagnostico del Sistema
echo ========================================
echo.

echo [1/6] Verificando instalacion de Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo [X] Java NO esta instalado o no esta en el PATH
    echo     Descarga Java 21 desde: https://www.oracle.com/java/technologies/downloads/
) else (
    echo [OK] Java instalado
    java -version 2>&1 | findstr "version"
)
echo.

echo [2/6] Verificando archivo JAR...
if exist "target\Farmacia-0.0.1-SNAPSHOT.jar" (
    echo [OK] Archivo JAR encontrado
    dir "target\Farmacia-0.0.1-SNAPSHOT.jar" | findstr "Farmacia"
) else (
    echo [X] Archivo JAR NO encontrado
    echo     Ejecuta: build.bat
)
echo.

echo [3/6] Verificando base de datos...
if exist "data\farmaciadb.mv.db" (
    echo [OK] Base de datos existe
    dir "data\farmaciadb.mv.db" | findstr "farmaciadb"
) else (
    echo [i] Base de datos NO existe (se creara al iniciar)
)
echo.

echo [4/6] Verificando puerto 8000...
netstat -ano | findstr ":8000" >nul
if errorlevel 1 (
    echo [OK] Puerto 8000 disponible
) else (
    echo [!] Puerto 8000 EN USO
    echo     La aplicacion puede estar corriendo o hay otro programa usando el puerto
    netstat -ano | findstr ":8000"
)
echo.

echo [5/6] Verificando procesos Java...
tasklist | findstr /I "java.exe" >nul
if errorlevel 1 (
    echo [i] No hay procesos Java corriendo
) else (
    echo [!] Procesos Java activos:
    tasklist | findstr /I "java.exe"
)
echo.

echo [6/6] Verificando scripts de inicio...
if exist "Iniciar_SaludSync.bat" (
    echo [OK] Iniciar_SaludSync.bat encontrado
) else (
    echo [X] Iniciar_SaludSync.bat NO encontrado
)

if exist "build.bat" (
    echo [OK] build.bat encontrado
) else (
    echo [X] build.bat NO encontrado
)

if exist "run.bat" (
    echo [OK] run.bat encontrado
) else (
    echo [X] run.bat NO encontrado
)
echo.

echo ========================================
echo   Resumen del Diagnostico
echo ========================================
echo.
echo Si todos los puntos estan en [OK], puedes ejecutar:
echo   Iniciar_SaludSync.bat
echo.
echo Si hay errores [X], sigue las recomendaciones mostradas.
echo.

pause

@echo off
title SaludSync - Crear Paquete Portable
color 0B

echo.
echo ========================================
echo   Crear Paquete Portable de SaludSync
echo ========================================
echo.

REM Verificar que existe el JAR
if not exist "target\Farmacia-0.0.1-SNAPSHOT.jar" (
    color 0C
    echo [ERROR] No se encontro el archivo JAR.
    echo.
    echo Debes compilar primero con: build.bat
    echo.
    pause
    exit /b 1
)

echo [INFO] Creando carpeta de distribucion...
echo.

REM Crear carpeta de distribución
if exist "SaludSync_Portable" (
    echo [INFO] Eliminando version anterior...
    rmdir /S /Q "SaludSync_Portable"
)

mkdir "SaludSync_Portable"

REM Copiar archivos necesarios
echo [INFO] Copiando archivo JAR...
copy "target\Farmacia-0.0.1-SNAPSHOT.jar" "SaludSync_Portable\" >nul

if errorlevel 1 (
    color 0C
    echo [ERROR] No se pudo copiar el JAR.
    pause
    exit /b 1
)

echo [INFO] Creando script de inicio...
REM Crear el script de inicio directamente
echo @echo off > "SaludSync_Portable\Iniciar_SaludSync.bat"
echo title SaludSync - Sistema de Turnos Medicos >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo color 0A >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo. >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo ======================================== >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo   SaludSync - Sistema de Turnos Medicos >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo ======================================== >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo. >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo [INFO] Iniciando aplicacion... >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo. >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo ======================================== >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo   Acceso a la aplicacion: >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo   http://localhost:8000 >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo. >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo   Usuario: admin >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo   Password: admin >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo. >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo   [!] Las advertencias (WARN) son normales >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo   [!] Espera a ver: "Started FarmaciaApplication" >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo   [!] Presiona Ctrl+C para detener >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo ======================================== >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo. >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo. >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo java -jar Farmacia-0.0.1-SNAPSHOT.jar >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo. >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo [INFO] La aplicacion se detuvo. >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo echo. >> "SaludSync_Portable\Iniciar_SaludSync.bat"
echo pause >> "SaludSync_Portable\Iniciar_SaludSync.bat"

if not exist "SaludSync_Portable\Iniciar_SaludSync.bat" (
    color 0C
    echo [ERROR] No se pudo crear el script de inicio.
    pause
    exit /b 1
)

echo [OK] Script de inicio creado

REM Preguntar si quiere incluir datos
echo.
echo [PREGUNTA] Deseas incluir la base de datos con los datos actuales?
echo.
echo   1. Si - Incluir datos (medicos, pacientes, turnos existentes)
echo   2. No - Base de datos vacia (solo usuario admin)
echo.
choice /C 12 /N /M "Selecciona una opcion (1 o 2): "

if errorlevel 2 goto :no_data
if errorlevel 1 goto :with_data

:with_data
if exist "data" (
    echo.
    echo [INFO] Copiando base de datos...
    xcopy "data" "SaludSync_Portable\data\" /E /I /Q >nul
    echo [OK] Base de datos incluida
) else (
    echo.
    echo [ADVERTENCIA] No se encontro carpeta data/
    echo Se creara una base de datos nueva al iniciar.
)
goto :create_readme

:no_data
echo.
echo [INFO] No se incluira la base de datos.
echo Se creara una nueva al iniciar (con usuario admin).

:create_readme
echo.
echo [INFO] Creando archivo README.txt...

REM Crear archivo README para la distribución
(
echo ========================================
echo   SaludSync - Sistema de Turnos Medicos
echo ========================================
echo.
echo COMO EJECUTAR:
echo   1. Asegurate de tener Java 21 o superior instalado
echo   2. Doble clic en: Iniciar_SaludSync.bat
echo   3. Espera a ver el mensaje: "Started FarmaciaApplication"
echo   4. Abre tu navegador en: http://localhost:8000
echo.
echo CREDENCIALES POR DEFECTO:
echo   Usuario: admin
echo   Password: admin
echo.
echo REQUISITOS:
echo   - Java 21 o superior
echo   - Windows
echo.
echo COMO VERIFICAR JAVA:
echo   Abre CMD o PowerShell y ejecuta: java -version
echo   Debe mostrar: java version "21" o superior
echo.
echo DESCARGAR JAVA:
echo   https://www.oracle.com/java/technologies/downloads/
echo.
echo PROBLEMAS COMUNES:
echo   - Si no funciona, verifica que Java este instalado
echo   - Si el puerto 8000 esta ocupado, cierra otras aplicaciones
echo   - NO hagas doble clic en el archivo .jar directamente
echo   - SIEMPRE usa el script Iniciar_SaludSync.bat
echo.
echo SOPORTE:
echo   Este es un sistema de gestion de turnos medicos
echo   desarrollado como proyecto educativo.
echo.
echo ========================================
) > "SaludSync_Portable\README.txt"

echo [OK] README.txt creado
echo.

REM Mostrar resumen
color 0A
echo.
echo ========================================
echo   [EXITO] Paquete Portable Creado
echo ========================================
echo.
echo Ubicacion: %CD%\SaludSync_Portable\
echo.
echo Contenido:
dir /B "SaludSync_Portable"
echo.
echo Puedes:
echo   - Copiar la carpeta a una USB
echo   - Comprimir en ZIP para enviar
echo   - Copiar a otro equipo directamente
echo.
echo En el equipo destino:
echo   1. Copiar la carpeta SaludSync_Portable
echo   2. Doble clic en Iniciar_SaludSync.bat
echo.

pause

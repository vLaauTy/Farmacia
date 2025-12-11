@echo off
echo ========================================
echo   SaludSync - Compilacion
echo ========================================
echo.
echo Compilando la aplicacion...
echo Este proceso puede tardar unos minutos...
echo.

REM Compilar con Maven
call mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   COMPILACION EXITOSA
    echo ========================================
    echo.
    echo El archivo JAR se encuentra en:
    echo target\Farmacia-0.0.1-SNAPSHOT.jar
    echo.
    echo Para ejecutar la aplicacion, usa: run.bat
    echo O ejecuta: java -jar target\Farmacia-0.0.1-SNAPSHOT.jar
    echo.
) else (
    echo.
    echo ========================================
    echo   ERROR EN LA COMPILACION
    echo ========================================
    echo.
    echo Revisa los errores anteriores.
    echo.
)

pause

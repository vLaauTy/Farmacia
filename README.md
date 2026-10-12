##  Requisitos
- Java 25 LTS o superior

##  Instalación y Ejecución

###  Opción 1: Inicio Rápido (Recomendado)

**Doble clic en:** `Iniciar_SaludSync.bat`

### Opción 2: Compilar y Ejecutar Paso a Paso

```bash
# 1. Compilar la aplicación (solo la primera vez o si cambias código)
build.bat

# 2. Ejecutar la aplicación
run.bat
```

### Opción 3: Crear Paquete Portable para Distribución

**Doble clic en:** `Crear_Paquete_Portable.bat`

Este script crea una carpeta `SaludSync_Portable/` lista para:
- ✅ Copiar a USB
- ✅ Enviar a otros equipos
- ✅ Comprimir en ZIP
- ✅ Incluye todo lo necesario para ejecutar

### 🔧 Opción 4: Ejecutar con Maven (Desarrolladores)
```bash
./mvnw spring-boot:run
```

##  Portabilidad - Cómo Llevar la Aplicación a Otro Equipo

###  Archivos Necesarios para Portabilidad:

Para ejecutar en otro dispositivo, necesitas copiar:

```
📁 SaludSync_Portable/
  ├── 📄 Farmacia-0.0.1-SNAPSHOT.jar    (obligatorio)
  ├── 📄 Iniciar_SaludSync.bat          (obligatorio)
  └── 📁 data/                          (opcional - solo si quieres mantener datos)
       └── farmaciadb.mv.db
```

###  Pasos para Distribución:


**En el otro equipo:**
1. Copia la carpeta `SaludSync_Portable` (incluye carpeta data/)
2. Doble clic en `Iniciar_SaludSync.bat`
3. Todos los médicos, pacientes y turnos estarán disponibles

###  Requisitos en el Equipo Destino:

## Confirmación de turnos por correo

Al crear un turno se envía una confirmación al email cargado en el paciente. El mensaje contiene solo la fecha, la hora y el profesional. La configuración de ejecución de VS Code lee las variables SMTP desde `.env`.

1. Copiá `.env.example` con el nombre `.env` en la raíz del proyecto.
2. En `.env`, reemplazá la cuenta de ejemplo y pegá la contraseña de aplicación de Gmail.
3. Reiniciá SaludSync desde VS Code y usá **Prueba de correo** en el panel administrador.

El archivo `.env` está excluido de Git: no compartas ni publiques sus credenciales. Para Gmail se necesita una contraseña de aplicación, no la contraseña normal. Si se inicia desde PowerShell con Maven, definí las mismas variables en esa terminal antes de ejecutar:

```powershell
$env:MAIL_HOST = 'smtp.gmail.com'
$env:MAIL_PORT = '587'
$env:MAIL_USERNAME = 'tu-cuenta@gmail.com'
$env:MAIL_PASSWORD = 'contraseña-de-aplicación'
$env:MAIL_FROM = 'tu-cuenta@gmail.com'
.\mvnw.cmd spring-boot:run
```

Si SMTP o el email del paciente no están configurados, el turno igualmente se guarda y la aplicación informa que el correo quedó pendiente.


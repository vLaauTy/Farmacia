# SaludSync

Les doy la bienvenida a mi proyecto final de carrera, llamado SaludSync.
Consiste en un sistema de turnos médicos, donde la persona encargada del consultorio coordina con el sistema el turno entre el médico y el paciente.
La idea es que se utilice de manera local: lo usan los médicos y la persona encargada de coordinar los turnos.

Sistema web de gestión de turnos médicos: agenda de turnos, pacientes con historial clínico, médicos con su propio acceso y confirmación de turnos por correo. Para el uso paso a paso, ver el [Manual de usuario](Manual%20de%20usuario.pdf).

**Tecnologías:** Java 25 · Spring Boot 4.1 (Spring MVC, Security, Data JPA) · Thymeleaf · base de datos H2 en archivo.

## Requisitos

- Java 25 o superior (`java -version` para verificarlo).
- No hace falta instalar Maven: el proyecto incluye `mvnw`.

## Ejecución

| Forma | Cómo |
|---|---|
| Doble clic (recomendado) | `build.bat` la primera vez (o después de cambiar código) y luego `Iniciar_SaludSync.bat` |
| VS Code | Abrir la carpeta y ejecutar con **F5** |
| Terminal | `.\mvnw.cmd spring-boot:run` |

Después abrir **http://localhost:8000**. Desde otra PC de la misma red se accede con la IP del equipo, por ejemplo `http://192.168.0.10:8000`.

### Usuarios

- **Administrador:** `admin` / `admin` (se crea automáticamente la primera vez).
- **Médicos:** cada médico registrado desde el panel recibe el usuario y la contraseña que se cargaron en su ficha.

El usuario distingue mayúsculas y minúsculas.

### Qué puede hacer cada rol

- **Administrador:** turnos, calendario semanal, pacientes, médicos, usuarios y prueba de correo.
- **Médico:** su agenda con resumen semanal, el listado de pacientes y la edición del historial clínico.

## Datos

La base se guarda en `data/farmaciadb.mv.db`, **relativa a la carpeta desde donde se ejecuta la aplicación**. `Iniciar_SaludSync.bat` y VS Code usan siempre esta carpeta del proyecto.

- Para hacer una copia de seguridad, copiar ese archivo con la aplicación cerrada.
- No ejecutar dos instancias a la vez: la base queda bloqueada y la segunda no arranca.

## Confirmación de turnos por correo

Al agendar un turno se envía un correo al email del paciente con la fecha, la hora y el profesional.

1. Copiar `.env.example` con el nombre `.env` en esta carpeta.
2. Completar la cuenta y la **contraseña de aplicación** de Gmail (no la contraseña normal).
3. Reiniciar SaludSync y probar desde **Prueba de correo** en el panel del administrador.

`Iniciar_SaludSync.bat` y VS Code leen el `.env` automáticamente. Si se usa la terminal, hay que definir las variables antes:

```powershell
$env:MAIL_HOST = 'smtp.gmail.com'
$env:MAIL_PORT = '587'
$env:MAIL_USERNAME = 'tu-cuenta@gmail.com'
$env:MAIL_PASSWORD = 'contraseña-de-aplicación'
$env:MAIL_FROM = 'tu-cuenta@gmail.com'
.\mvnw.cmd spring-boot:run
```

El archivo `.env` está excluido de Git: no compartas sus credenciales. Si el correo no está configurado o el paciente no tiene email, el turno se guarda igual y la aplicación avisa que el correo quedó pendiente.

## Llevar la aplicación a otro equipo

Copiar en una carpeta:

```
SaludSync/
├── Farmacia-0.0.1-SNAPSHOT.jar   (de target/, generado con build.bat)
├── Iniciar_Aplicacion.bat         (está en la carpeta SaludSync_distribucion)
├── .env                          (opcional: configuración de correo)
└── data/                         (opcional: solo para llevar los datos actuales)
    └── farmaciadb.mv.db
```

En el otro equipo solo hace falta Java 25. Sin la carpeta `data/`, la aplicación arranca vacía con el usuario `admin`.

## Tests

```powershell
.\mvnw.cmd test
```

Los tests levantan la aplicación completa con una base en memoria, así que **no modifican los datos reales**. Cubren permisos por rol, validaciones de turnos, alta y edición de médicos con su usuario, y la edición del historial clínico.

## Estructura del código

```
src/main/java/Farmacia/Farmacia/
├── config/       Seguridad: login y permisos por rol (UserConfig)
├── controller/   Rutas web y armado de cada pantalla
├── Service/      Reglas del negocio (disponibilidad de turnos, cuentas de médicos, correo)
├── Repository/   Acceso a la base de datos (Spring Data JPA)
└── Model/        Entidades: Turnos, Paciente, Medico, UserModel
src/main/resources/
├── templates/    Pantallas Thymeleaf (fragments/layout.html tiene el menú común)
└── static/       Estilos, iconos y JavaScript
```



##  Requisitos
- Java 21 o superior

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

### �🔧 Opción 4: Ejecutar con Maven (Desarrolladores)
```bash
./mvnw spring-boot:run
```

## 📦 Portabilidad - Cómo Llevar la Aplicación a Otro Equipo

### 📁 Archivos Necesarios para Portabilidad:

Para ejecutar en otro dispositivo, necesitas copiar:

```
📁 SaludSync_Portable/
  ├── 📄 Farmacia-0.0.1-SNAPSHOT.jar    (obligatorio)
  ├── 📄 Iniciar_SaludSync.bat          (obligatorio)
  └── 📁 data/                          (opcional - solo si quieres mantener datos)
       └── farmaciadb.mv.db
```

### 🚀 Pasos para Distribución:


**En el otro equipo:**
1. Copia la carpeta `SaludSync_Portable` (incluye carpeta data/)
2. Doble clic en `Iniciar_SaludSync.bat`
3. Todos los médicos, pacientes y turnos estarán disponibles

### ⚠️ Requisitos en el Equipo Destino:
- ✅ Java 21 o superior instalado
- ✅ Windows (los scripts .bat son para Windows)


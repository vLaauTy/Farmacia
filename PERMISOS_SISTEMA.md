# 🔐 Control de Acceso - Sistema de Turnos

## 📋 Permisos por Rol

### 👨‍⚕️ **MÉDICO**
✅ **Permitido:**
- `/mis-turnos` - Ver solo sus turnos asignados
- `/pacientes` - Ver lista de pacientes 
- Menú médico personalizado

❌ **Restringido:**
- `/calendario` - No puede ver el calendario completo
- `/turnos` - No puede ver todos los turnos
- `/turnos/gestion` - No puede gestionar turnos
- `/turnos/nuevo` - No puede crear turnos
- `/turnos/editar/*` - No puede editar turnos

### 🔧 **ADMINISTRADOR**
✅ **Acceso completo:**
- `/calendario` - Calendario semanal completo
- `/turnos` - Lista de todos los turnos
- `/turnos/gestion` - Gestión completa de turnos
- `/turnos/nuevo` - Crear nuevos turnos
- `/turnos/editar/*` - Editar cualquier turno
- `/mis-turnos` - Ver turnos (si fuera médico también)
- Gestión de médicos y pacientes

## 🎯 Implementación de Seguridad

### Endpoints protegidos:
```java
// Solo ADMIN
@GetMapping("/calendario")          // + Authentication check
@GetMapping("/turnos")             // + Authentication check  
@GetMapping("/turnos/gestion")     // + Authentication check

// Accesible para ambos roles
@GetMapping("/mis-turnos")         // Filtra por usuario autenticado
@GetMapping("/pacientes")          // Acceso general
```

### Controles de Vista:
```html
<!-- Solo visible para ADMIN -->
<div sec:authorize="hasRole('ADMIN')">
    <a href="/calendario">📅 Ver Calendario</a>
</div>
```

## 🔒 Redirecciones de Seguridad
- **Acceso denegado:** `redirect:/login?error=access_denied`
- **Sin autenticación:** `redirect:/login`

## ✅ Resultado Final
- **Médicos:** Solo ven sus propios turnos en formato lista
- **Administradores:** Ven todo el sistema incluyendo calendario completo
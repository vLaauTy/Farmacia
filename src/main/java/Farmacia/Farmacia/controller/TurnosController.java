package Farmacia.Farmacia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Duration;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import Farmacia.Farmacia.Model.Turnos;
import Farmacia.Farmacia.Model.Medico;
import Farmacia.Farmacia.Model.Paciente;
import Farmacia.Farmacia.Service.TurnosService;
import Farmacia.Farmacia.Service.MedicoService;
import Farmacia.Farmacia.Service.PacienteService;

@Controller
@RequestMapping
public class TurnosController {

    @Autowired
    private TurnosService turnosService;

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private PacienteService pacienteService;

    @GetMapping("/turnos/gestion")
    public String gestionTurnos(Authentication authentication) {
        // Verificar que el usuario sea administrador
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }
        return "gestion_turnos";
    }

    @GetMapping("/turnos")
    public String listarTurnos(Model model, Authentication authentication) {
        // Verificar que el usuario sea administrador
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }
        
        model.addAttribute("turnos", turnosService.obtenerTurnos());
        return "turnos";
    }

    @GetMapping("/turnos/nuevo")
    public String mostrarFormularioTurno(Model model, @RequestParam(required = false) Long medicoId) {
        model.addAttribute("turno", new Turnos());
        model.addAttribute("medicos", medicoService.getAllMedicos());
        model.addAttribute("pacientes", pacienteService.getAllPacientes());

        // Si se seleccionó un médico, cargar sus horarios
        if (medicoId != null) {
            Medico medicoSeleccionado = medicoService.getMedicoById(medicoId);
            if (medicoSeleccionado != null) {
                model.addAttribute("medicoSeleccionado", medicoSeleccionado);
                model.addAttribute("horariosDisponibles", generarHorariosPorTipo(medicoSeleccionado.getHorario()));
            }
        }

        return "formulario_turno";
    }

    @PostMapping("/turnos/guardar")
    public String guardarTurno(@ModelAttribute Turnos turno,
            @RequestParam(name = "medico", required = false) Long medicoId,
            @RequestParam(name = "paciente", required = false) Long pacienteId,
            Model model) {
        try {
            // Validar fecha (debe ser al menos mañana)
            LocalDate hoy = LocalDate.now();
            LocalDate fechaTurno = LocalDate.of(turno.getAnio(), turno.getMes(), turno.getDia());
            
            if (!fechaTurno.isAfter(hoy)) {
                model.addAttribute("error", "Los turnos deben programarse con al menos un día de anterioridad");
                model.addAttribute("turno", turno);
                model.addAttribute("medicos", medicoService.getAllMedicos());
                model.addAttribute("pacientes", pacienteService.getAllPacientes());
                return "formulario_turno";
            }

            // Establecer el médico desde el ID
            if (medicoId != null) {
                Medico medico = medicoService.getMedicoById(medicoId);
                turno.setMedico(medico);
            }

            // Establecer el paciente desde el ID
            if (pacienteId != null) {
                Paciente paciente = pacienteService.getPacienteById(pacienteId);
                turno.setPaciente(paciente);
            }

            // Verificar que el paciente no tenga otro turno en la misma fecha y hora
            if (pacienteId != null && turnosService.existeTurnoPaciente(pacienteId, turno.getDia(), turno.getMes(), turno.getAnio(), turno.getHora())) {
                model.addAttribute("error", "El paciente ya tiene un turno programado para esta fecha y hora");
                model.addAttribute("turno", turno);
                model.addAttribute("medicos", medicoService.getAllMedicos());
                model.addAttribute("pacientes", pacienteService.getAllPacientes());
                return "formulario_turno";
            }

            turnosService.guardarTurno(turno);
            return "redirect:/turnos";
        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar el turno: " + e.getMessage());
            model.addAttribute("turno", turno);
            model.addAttribute("medicos", medicoService.getAllMedicos());
            model.addAttribute("pacientes", pacienteService.getAllPacientes());
            return "formulario_turno";
        }
    }

    @GetMapping("/turnos/editar/{id}")
    public String editarTurno(@PathVariable Long id, Model model) {
        var turno = turnosService.obtenerPorId(id);
        if (turno.isPresent()) {
            Turnos turnoObj = turno.get();
            model.addAttribute("turno", turnoObj);
            model.addAttribute("medicos", medicoService.getAllMedicos());
            model.addAttribute("pacientes", pacienteService.getAllPacientes());

            // Si el turno ya tiene un médico asignado, mostrar sus horarios
            if (turnoObj.getMedico() != null) {
                model.addAttribute("medicoSeleccionado", turnoObj.getMedico());
                model.addAttribute("horariosDisponibles", generarHorariosPorTipo(turnoObj.getMedico().getHorario()));
            }

            return "formulario_turno";
        }
        return "redirect:/turnos";
    }

    @PostMapping("/turnos/editar/{id}")
    public String actualizarTurno(@PathVariable Long id,
            @ModelAttribute Turnos turno,
            @RequestParam(name = "medico", required = false) Long medicoId,
            @RequestParam(name = "paciente", required = false) Long pacienteId,
            Model model) {
        try {
            turno.setId(id);

            // Validar fecha (debe ser al menos mañana)
            LocalDate hoy = LocalDate.now();
            LocalDate fechaTurno = LocalDate.of(turno.getAnio(), turno.getMes(), turno.getDia());
            
            if (!fechaTurno.isAfter(hoy)) {
                model.addAttribute("error", "Los turnos deben programarse con al menos un día de anterioridad");
                model.addAttribute("turno", turno);
                model.addAttribute("medicos", medicoService.getAllMedicos());
                model.addAttribute("pacientes", pacienteService.getAllPacientes());
                return "formulario_turno";
            }

            // Establecer el médico desde el ID
            if (medicoId != null) {
                Medico medico = medicoService.getMedicoById(medicoId);
                turno.setMedico(medico);
            }

            // Establecer el paciente desde el ID
            if (pacienteId != null) {
                Paciente paciente = pacienteService.getPacienteById(pacienteId);
                turno.setPaciente(paciente);
            }

            // Verificar que el paciente no tenga otro turno en la misma fecha y hora (excluyendo el turno actual)
            if (pacienteId != null && turnosService.existeTurnoPacienteExcluyendoId(pacienteId, turno.getDia(), turno.getMes(), turno.getAnio(), turno.getHora(), id)) {
                model.addAttribute("error", "El paciente ya tiene un turno programado para esta fecha y hora");
                model.addAttribute("turno", turno);
                model.addAttribute("medicos", medicoService.getAllMedicos());
                model.addAttribute("pacientes", pacienteService.getAllPacientes());
                return "formulario_turno";
            }

            turnosService.guardarTurno(turno);
            return "redirect:/turnos";
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar el turno: " + e.getMessage());
            model.addAttribute("turno", turno);
            model.addAttribute("medicos", medicoService.getAllMedicos());
            model.addAttribute("pacientes", pacienteService.getAllPacientes());
            return "formulario_turno";
        }
    }

    @GetMapping("/turnos/eliminar/{id}")
    public String eliminarTurno(@PathVariable Long id) {
        turnosService.eliminarTurno(id);
        return "redirect:/turnos";
    }

    @GetMapping("/turnos/horarios/{medicoId}")
    @ResponseBody
    public ResponseEntity<List<String>> obtenerHorarios(@PathVariable Long medicoId) {
        try {
            Medico medico = medicoService.getMedicoById(medicoId);

            List<String> horarios = generarHorariosPorTipo(medico.getHorario());
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Endpoint para obtener turnos ocupados - usado por JavaScript para filtrar
    @GetMapping("/turnos/ocupados")
    @ResponseBody
    public ResponseEntity<List<Turnos>> obtenerTurnosOcupados() {
        try {
            return ResponseEntity.ok(turnosService.obtenerTurnos());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Endpoint para que los médicos vean sus turnos asignados
    @GetMapping("/mis-turnos")
    public String misTurnos(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {/* Lines 230-245 omitted */}

        return "redirect:/login";
    }

    // Endpoint para mostrar calendario semanal de turnos (solo para ADMIN)
    @GetMapping("/calendario")
    public String mostrarCalendario(Model model, Authentication authentication) {
        // Verificar que el usuario sea administrador
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }
        
        LocalDate hoy = LocalDate.now();
        
        // Generar los próximos 7 días empezando desde hoy
        List<LocalDate> diasSemana = Stream.iterate(hoy, fecha -> fecha.plusDays(1))
                .limit(7)
                .collect(Collectors.toList());
        
        // Generar horarios cada 30 minutos de 8:00 a 20:00
        List<String> horarios = generarHorarios(LocalTime.of(8, 0), LocalTime.of(20, 0));
        
        // Obtener todos los turnos de estos 7 días
        List<Turnos> turnosSemanales = turnosService.obtenerTurnosEntreFechas(hoy, hoy.plusDays(6));
        
        // Crear un mapa para búsqueda rápida de turnos
        java.util.Map<String, String> calendarioTurnos = new java.util.HashMap<>();
        for (Turnos turno : turnosSemanales) {
            String clave = turno.getDia() + "-" + turno.getMes() + "-" + turno.getAnio() + "-" + turno.getHora();
            String valor = "Dr. " + turno.getMedico().getApellido() + "|" + 
                          turno.getMedico().getNombre() + " " + turno.getMedico().getApellido() + "|" +
                          turno.getPaciente().getNombre() + " " + turno.getPaciente().getApellido();
            calendarioTurnos.put(clave, valor);
        }
        
        model.addAttribute("diasSemana", diasSemana);
        model.addAttribute("horarios", horarios);
        model.addAttribute("turnos", turnosSemanales);
        model.addAttribute("calendarioTurnos", calendarioTurnos);
        
        return "calendario_turnos";
    }    // Métodos auxiliares para generar horarios
    private List<String> generarHorariosPorTipo(String horario) {
        return switch (horario) {
            case "Mañana (8:00 - 14:00)" -> generarHorarios(LocalTime.of(8, 0), LocalTime.of(14, 0));
            case "Tarde (14:00 - 20:00)" -> generarHorarios(LocalTime.of(14, 0), LocalTime.of(20, 0));
            case "Tiempo Completo (8:00 - 20:00)" -> generarHorarios(LocalTime.of(8, 0), LocalTime.of(20, 0));
            default -> List.of();
        };
    }

    private List<String> generarHorarios(LocalTime inicio, LocalTime fin) {
        return Stream.iterate(inicio, time -> time.isBefore(fin), time -> time.plus(Duration.ofMinutes(30)))
                .map(time -> time.format(DateTimeFormatter.ofPattern("HH:mm")))
                .toList();
    }
}

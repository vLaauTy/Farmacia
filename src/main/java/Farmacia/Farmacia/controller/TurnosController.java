package Farmacia.Farmacia.controller;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import Farmacia.Farmacia.Model.Medico;
import Farmacia.Farmacia.Model.Paciente;
import Farmacia.Farmacia.Model.Turnos;
import Farmacia.Farmacia.Service.MedicoService;
import Farmacia.Farmacia.Service.PacienteService;
import Farmacia.Farmacia.Service.TurnoEmailService;
import Farmacia.Farmacia.Service.TurnosService;

@Controller
public class TurnosController {

    // Datos mínimos de un turno ocupado que usa el formulario para marcar horarios no disponibles
    public record TurnoOcupado(Long id, Long medicoId, Long pacienteId, Integer dia, Integer mes, Integer anio,
            String hora) {

        static TurnoOcupado de(Turnos turno) {
            return new TurnoOcupado(turno.getId(),
                    turno.getMedico() != null ? turno.getMedico().getId() : null,
                    turno.getPaciente() != null ? turno.getPaciente().getId() : null,
                    turno.getDia(), turno.getMes(), turno.getAnio(), turno.getHora());
        }
    }

    private final TurnosService turnosService;
    private final MedicoService medicoService;
    private final PacienteService pacienteService;
    private final TurnoEmailService turnoEmailService;

    public TurnosController(TurnosService turnosService, MedicoService medicoService,
            PacienteService pacienteService, TurnoEmailService turnoEmailService) {
        this.turnosService = turnosService;
        this.medicoService = medicoService;
        this.pacienteService = pacienteService;
        this.turnoEmailService = turnoEmailService;
    }

    // ---------- Listado y formulario (solo ADMIN, ver UserConfig) ----------

    @GetMapping("/turnos")
    public String listarTurnos(Model model) {
        model.addAttribute("turnos", turnosService.obtenerTurnosActivos());
        model.addAttribute("turnosCanceladosDelMes", turnosService.obtenerTurnosCanceladosDelMes());
        model.addAttribute("nombreMes", turnosService.obtenerNombreDelMes());
        return "turnos";
    }

    @GetMapping("/turnos/nuevo")
    public String nuevoTurno(Model model) {
        return mostrarFormulario(model, new Turnos(), null);
    }

    @PostMapping("/turnos/guardar")
    public String guardarTurno(@ModelAttribute Turnos turno,
            @RequestParam(name = "medico", required = false) Long medicoId,
            @RequestParam(name = "paciente", required = false) Long pacienteId,
            Model model, RedirectAttributes redirectAttributes) {
        String error = completarYValidar(turno, medicoId, pacienteId);
        if (error != null) {
            return mostrarFormulario(model, turno, error);
        }

        turnosService.guardarTurno(turno);
        Optional<String> errorCorreo = turnoEmailService.enviarConfirmacion(turno);
        if (errorCorreo.isPresent()) {
            redirectAttributes.addFlashAttribute("correoError", errorCorreo.get());
            return "redirect:/turnos?correo=pendiente";
        }
        return "redirect:/turnos?correo=enviado";
    }

    @GetMapping("/turnos/editar/{id}")
    public String editarTurno(@PathVariable Long id, Model model) {
        return turnosService.obtenerPorId(id)
                .map(turno -> mostrarFormulario(model, turno, null))
                .orElse("redirect:/turnos");
    }

    @PostMapping("/turnos/editar/{id}")
    public String actualizarTurno(@PathVariable Long id, @ModelAttribute Turnos turno,
            @RequestParam(name = "medico", required = false) Long medicoId,
            @RequestParam(name = "paciente", required = false) Long pacienteId,
            Model model) {
        turno.setId(id);
        String error = completarYValidar(turno, medicoId, pacienteId);
        if (error != null) {
            return mostrarFormulario(model, turno, error);
        }

        turnosService.guardarTurno(turno);
        return "redirect:/turnos";
    }

    @GetMapping("/turnos/eliminar/{id}")
    public String cancelarTurno(@PathVariable Long id) {
        turnosService.cancelarTurno(id);
        return "redirect:/turnos?cancelado=true";
    }

    // ---------- Datos para el formulario (JSON) ----------

    @GetMapping("/turnos/horarios/{medicoId}")
    @ResponseBody
    public ResponseEntity<List<String>> obtenerHorarios(@PathVariable Long medicoId) {
        Medico medico = medicoService.getMedicoById(medicoId);
        if (medico == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(turnosService.horariosDelMedico(medico));
    }

    @GetMapping("/turnos/ocupados")
    @ResponseBody
    public List<TurnoOcupado> obtenerTurnosOcupados() {
        return turnosService.obtenerTurnosActivos().stream()
                .map(TurnoOcupado::de)
                .toList();
    }

    // ---------- Vistas de agenda ----------

    // Agenda del médico que inició sesión (solo MEDICO, ver UserConfig)
    @GetMapping("/mis-turnos")
    public String misTurnos(Model model, Authentication authentication) {
        Medico medico = medicoService.getMedicoByUsername(authentication.getName());
        if (medico == null) {
            return "redirect:/";
        }

        List<Turnos> turnos = turnosService.obtenerTurnosPorMedico(medico.getId());
        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(DayOfWeek.MONDAY);
        LocalDate domingo = hoy.with(DayOfWeek.SUNDAY);

        model.addAttribute("medico", medico);
        model.addAttribute("turnos", turnos);
        model.addAttribute("turnosSemanaAnterior", contarEntre(turnos, lunes.minusWeeks(1), domingo.minusWeeks(1)));
        model.addAttribute("turnosSemanaActual", contarEntre(turnos, lunes, domingo));
        model.addAttribute("turnosRestantesSemana", contarEntre(turnos, hoy, domingo));
        return "mis_turnos_medico";
    }

    // Grilla de los próximos 8 días (solo ADMIN, ver UserConfig)
    @GetMapping("/calendario")
    public String mostrarCalendario(Model model) {
        LocalDate hoy = LocalDate.now();
        LocalDate ultimoDia = hoy.plusDays(7);
        List<Turnos> turnos = turnosService.obtenerTurnosEntreFechas(hoy, ultimoDia);

        // Clave "aaaa-mm-dd HH:mm" -> turnos de ese día y horario
        Map<String, List<Turnos>> calendarioTurnos = new HashMap<>();
        for (Turnos turno : turnos) {
            String clave = turno.getFecha() + " " + turno.getHora();
            calendarioTurnos.computeIfAbsent(clave, k -> new ArrayList<>()).add(turno);
        }

        model.addAttribute("diasSemana", hoy.datesUntil(ultimoDia.plusDays(1)).toList());
        model.addAttribute("horarios", turnosService.generarHorarios(LocalTime.of(8, 0), LocalTime.of(20, 0)));
        model.addAttribute("turnos", turnos);
        model.addAttribute("calendarioTurnos", calendarioTurnos);
        return "calendario_turnos";
    }

    // ---------- Auxiliares ----------

    private String mostrarFormulario(Model model, Turnos turno, String error) {
        model.addAttribute("turno", turno);
        model.addAttribute("medicos", medicoService.getAllMedicos());
        model.addAttribute("pacientes", pacienteService.getAllPacientes());
        model.addAttribute("error", error);
        return "formulario_turno";
    }

    // Asigna médico y paciente al turno y devuelve el mensaje de error, o null si es válido
    private String completarYValidar(Turnos turno, Long medicoId, Long pacienteId) {
        Medico medico = medicoId != null ? medicoService.getMedicoById(medicoId) : null;
        Paciente paciente = pacienteId != null ? pacienteService.getPacienteById(pacienteId) : null;
        turno.setMedico(medico);
        turno.setPaciente(paciente);

        if (medico == null) {
            return "Seleccioná un médico.";
        }
        if (paciente == null) {
            return "Seleccioná un paciente.";
        }
        if (turno.getHora() == null || turno.getHora().isBlank()) {
            return "Seleccioná un horario.";
        }

        LocalDate fecha;
        try {
            fecha = turno.getFecha();
        } catch (DateTimeException e) {
            fecha = null; // por ejemplo, 31 de febrero
        }
        if (fecha == null) {
            return "La fecha del turno no es válida.";
        }
        if (!fecha.isAfter(LocalDate.now())) {
            return "Los turnos deben programarse con al menos un día de anticipación.";
        }
        if (turnosService.medicoOcupado(medico.getId(), fecha, turno.getHora(), turno.getId())) {
            return "El médico ya tiene un turno en esa fecha y horario.";
        }
        if (turnosService.pacienteOcupado(paciente.getId(), fecha, turno.getHora(), turno.getId())) {
            return "El paciente ya tiene un turno programado para esta fecha y hora.";
        }
        return null;
    }

    private static long contarEntre(List<Turnos> turnos, LocalDate desde, LocalDate hasta) {
        return turnos.stream()
                .map(Turnos::getFecha)
                .filter(fecha -> !fecha.isBefore(desde) && !fecha.isAfter(hasta))
                .count();
    }
}

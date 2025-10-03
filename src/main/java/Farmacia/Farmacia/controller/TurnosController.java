package Farmacia.Farmacia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

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
    public String gestionTurnos() {
        return "gestion_turnos";
    }

    @GetMapping("/turnos")
    public String listarTurnos(Model model) {
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
    
    // Endpoint AJAX para obtener horarios de un médico
    @GetMapping("/turnos/horarios/{medicoId}")
    @ResponseBody
    public ResponseEntity<List<String>> obtenerHorariosMedico(@PathVariable Long medicoId) {
        try {
            Medico medico = medicoService.getMedicoById(medicoId);
            if (medico != null) {
                List<String> horarios = generarHorariosPorTipo(medico.getHorario());
                return ResponseEntity.ok(horarios);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Métodos auxiliares para generar horarios usando Java 8+ Stream API
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

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
@RequestMapping("/turnos")
public class TurnosController {

    @Autowired
    private TurnosService turnosService;

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private PacienteService pacienteService;

    @GetMapping("/gestion")
    public String gestionTurnos() {
        return "gestion_turnos";
    }

    @GetMapping
    public String listarTurnos(Model model) {
        model.addAttribute("turnos", turnosService.obtenerTurnos());
        return "turnos";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioTurno(Model model,
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) Integer dia,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio) {
        return prepararFormularioTurno(model, new Turnos(), medicoId, dia, mes, anio);
    }

    @GetMapping("/formulario")
    public String mostrarFormularioConFiltros(Model model,
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) Integer dia,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Long turnoId) {

        Turnos turno = new Turnos();
        if (turnoId != null) {
            var turnoOpt = turnosService.obtenerPorId(turnoId);
            if (turnoOpt.isPresent()) {
                turno = turnoOpt.get();
            }
        }

        return prepararFormularioTurno(model, turno, medicoId, dia, mes, anio);
    }

    @PostMapping("/guardar")
    public String guardarTurno(@ModelAttribute Turnos turno,
            @RequestParam(name = "medico", required = false) Long medicoId,
            @RequestParam(name = "paciente", required = false) Long pacienteId,
            Model model) {
        try {
            establecerRelaciones(turno, medicoId, pacienteId);
            turnosService.guardarTurno(turno);
            return "redirect:/turnos";
        } catch (Exception e) {
            return manejarErrorFormulario(model, turno, "Error al guardar el turno: " + e.getMessage());
        }
    }

    @GetMapping("/editar/{id}")
    public String editarTurno(@PathVariable Long id, Model model,
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) Integer dia,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio) {
        var turnoOpt = turnosService.obtenerPorId(id);
        if (turnoOpt.isPresent()) {
            Turnos turno = turnoOpt.get();

            // Si no se proporcionan parámetros, usar los del turno existente
            Long medicoIdFinal = medicoId != null ? medicoId
                    : (turno.getMedico() != null ? turno.getMedico().getId() : null);
            Integer diaFinal = dia != null ? dia : turno.getDia();
            Integer mesFinal = mes != null ? mes : turno.getMes();
            Integer anioFinal = anio != null ? anio : turno.getAnio();

            return prepararFormularioTurno(model, turno, medicoIdFinal, diaFinal, mesFinal, anioFinal);
        }
        return "redirect:/turnos";
    }

    @PostMapping("/editar/{id}")
    public String actualizarTurno(@PathVariable Long id,
            @ModelAttribute Turnos turno,
            @RequestParam(name = "medico", required = false) Long medicoId,
            @RequestParam(name = "paciente", required = false) Long pacienteId,
            Model model) {
        try {
            turno.setId(id);
            establecerRelaciones(turno, medicoId, pacienteId);
            turnosService.guardarTurno(turno);
            return "redirect:/turnos";
        } catch (Exception e) {
            return manejarErrorFormulario(model, turno, "Error al actualizar el turno: " + e.getMessage());
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarTurno(@PathVariable Long id) {
        turnosService.eliminarTurno(id);
        return "redirect:/turnos";
    }

    // Endpoint AJAX para obtener horarios disponibles de un médico en una fecha
    // específica
    @GetMapping("/horarios/{medicoId}")
    @ResponseBody
    public ResponseEntity<List<String>> obtenerHorariosDisponibles(
            @PathVariable Long medicoId,
            @RequestParam(required = false) Integer dia,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Long turnoId) {
        try {
            Medico medico = medicoService.getMedicoById(medicoId);
            if (medico == null) {
                return ResponseEntity.notFound().build();
            }

            // Generar todos los horarios posibles para el médico
            List<String> todosLosHorarios = generarHorariosPorTipo(medico.getHorario());

            // Si no se proporciona fecha, devolver todos los horarios
            if (dia == null || mes == null || anio == null) {
                return ResponseEntity.ok(todosLosHorarios);
            }

            // Filtrar horarios ocupados para la fecha específica
            List<String> horariosDisponibles = todosLosHorarios.stream()
                    .filter(hora -> {
                        // Si estamos editando un turno, permitir la hora actual del turno
                        if (turnoId != null) {
                            var turnoActual = turnosService.obtenerPorId(turnoId);
                            if (turnoActual.isPresent()) {
                                Turnos turno = turnoActual.get();
                                if (turno.getMedico().getId().equals(medicoId) &&
                                        turno.getDia().equals(dia) &&
                                        turno.getMes().equals(mes) &&
                                        turno.getAnio().equals(anio) &&
                                        turno.getHora().equals(hora)) {
                                    return true; // Permitir la hora del turno actual en edición
                                }
                            }
                        }

                        // Verificar si la hora está ocupada
                        return !turnosService.existeTurnoEnFechaYHora(medicoId, dia, mes, anio, hora);
                    })
                    .toList();

            return ResponseEntity.ok(horariosDisponibles);
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

    // MÉTODOS AUXILIARES OPTIMIZADOS

    // Método reutilizable para preparar formularios (elimina duplicación)
    private String prepararFormularioTurno(Model model, Turnos turno, Long medicoId,
            Integer dia, Integer mes, Integer anio) {
        model.addAttribute("turno", turno);
        cargarDatosComunes(model);

        // Agregar los parámetros de filtro al modelo
        if (medicoId != null)
            model.addAttribute("medicoSeleccionado", medicoId);
        if (dia != null)
            model.addAttribute("diaSeleccionado", dia);
        if (mes != null)
            model.addAttribute("mesSeleccionado", mes);
        if (anio != null)
            model.addAttribute("anioSeleccionado", anio);

        // Si hay un médico y fecha seleccionados, cargar horarios disponibles
        if (medicoId != null && dia != null && mes != null && anio != null) {
            Medico medico = medicoService.getMedicoById(medicoId);
            if (medico != null) {
                // Generar todos los horarios del médico
                List<String> todosLosHorarios = generarHorariosPorTipo(medico.getHorario());

                // Filtrar horarios disponibles para la fecha específica
                Long turnoId = turno.getId(); // Para excluir el turno actual si estamos editando
                List<String> horariosDisponibles = todosLosHorarios.stream()
                        .filter(hora -> {
                            // Si estamos editando, permitir la hora actual del turno
                            if (turnoId != null && turno.getMedico() != null &&
                                    turno.getMedico().getId().equals(medicoId) &&
                                    turno.getDia() != null && turno.getDia().equals(dia) &&
                                    turno.getMes() != null && turno.getMes().equals(mes) &&
                                    turno.getAnio() != null && turno.getAnio().equals(anio) &&
                                    turno.getHora() != null && turno.getHora().equals(hora)) {
                                return true;
                            }

                            // Verificar si la hora está disponible
                            return !turnosService.existeTurnoEnFechaYHora(medicoId, dia, mes, anio, hora);
                        })
                        .toList();

                model.addAttribute("horariosDisponibles", horariosDisponibles);
            }
        } else if (medicoId != null) {
            // Si solo hay médico seleccionado, mostrar todos sus horarios
            Medico medico = medicoService.getMedicoById(medicoId);
            if (medico != null) {
                model.addAttribute("horariosDisponibles", generarHorariosPorTipo(medico.getHorario()));
            }
        }

        return "formulario_turno";
    }

    // Sobrecarga del método para mantener compatibilidad

    // Método para cargar datos comunes en formularios
    private void cargarDatosComunes(Model model) {
        model.addAttribute("medicos", medicoService.getAllMedicos());
        model.addAttribute("pacientes", pacienteService.getAllPacientes());
    }

    // Método optimizado para manejar errores en formularios
    private String manejarErrorFormulario(Model model, Turnos turno, String mensajeError) {
        model.addAttribute("error", mensajeError);
        Long medicoId = turno.getMedico() != null ? turno.getMedico().getId() : null;
        return prepararFormularioTurno(model, turno, medicoId, turno.getDia(), turno.getMes(), turno.getAnio());
    }

    // Método para establecer relaciones de entidades
    private void establecerRelaciones(Turnos turno, Long medicoId, Long pacienteId) {
        if (medicoId != null) {
            Medico medico = medicoService.getMedicoById(medicoId);
            if (medico != null)
                turno.setMedico(medico);
        }
        if (pacienteId != null) {
            Paciente paciente = pacienteService.getPacienteById(pacienteId);
            if (paciente != null)
                turno.setPaciente(paciente);
        }
    }
}

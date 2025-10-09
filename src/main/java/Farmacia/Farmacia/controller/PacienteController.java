package Farmacia.Farmacia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import Farmacia.Farmacia.Model.Paciente;
import Farmacia.Farmacia.Service.PacienteService;

@Controller
@RequestMapping
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @GetMapping("/pacientes/gestion")
    public String gestionPacientes(Model model) {
        return "gestion_paciente";
    }

    @GetMapping("/pacientes")
    public String listarPacientes(Model model) {
        model.addAttribute("pacientes", pacienteService.getAllPacientes());
        return "pacientes";
    }

    @GetMapping("/pacientes/nuevo")
    public String mostrarFormularioNuevoPaciente(Model model, Authentication authentication) {
        // Verificar si el usuario es médico
        boolean esMedico = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MEDICO"));

        model.addAttribute("esMedico", esMedico);

        // Si no hay paciente en el modelo (primera carga), crear uno nuevo
        if (!model.containsAttribute("paciente")) {
            model.addAttribute("paciente", new Paciente());
        }

        return "formulario_paciente";
    }

    @PostMapping("/pacientes/guardar")
    public String guardarPaciente(@ModelAttribute("paciente") Paciente paciente, Model model,
            Authentication authentication) {
        // Verificar si ya existe un paciente con el mismo DNI
        if (pacienteService.existePacientePorDni(paciente.getDni())) {
            // Verificar si el usuario es médico para mantener el estado del formulario
            boolean esMedico = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MEDICO"));

            model.addAttribute("esMedico", esMedico);
            model.addAttribute("paciente", paciente);
            model.addAttribute("error", "Ya existe un paciente registrado con el DNI: " + paciente.getDni());
            return "formulario_paciente";
        }

        pacienteService.savePaciente(paciente);
        return "redirect:/pacientes?exito=paciente_guardado";
    }

    @GetMapping("/pacientes/editar/{id}")
    public String mostrarFormularioEditarPaciente(@PathVariable("id") Long id, Model model,
            Authentication authentication) {
        // Verificar si el usuario es médico
        boolean esMedico = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MEDICO"));

        model.addAttribute("esMedico", esMedico);
        model.addAttribute("paciente", pacienteService.getPacienteById(id));
        return "formulario_paciente";
    }

    @PostMapping("/pacientes/editar/{id}")
    public String guardarEdicionPaciente(@PathVariable("id") Long id, @ModelAttribute("paciente") Paciente paciente,
            Authentication authentication, Model model) {
        // Verificar si el usuario es médico
        boolean esMedico = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MEDICO"));

        // Si no es médico, validar DNI duplicado (médicos no pueden cambiar datos
        // personales)
        if (!esMedico && pacienteService.existePacientePorDniExcluyendoId(paciente.getDni(), id)) {
            model.addAttribute("esMedico", esMedico);
            model.addAttribute("paciente", paciente);
            model.addAttribute("error", "Ya existe otro paciente registrado con el DNI: " + paciente.getDni());
            return "formulario_paciente";
        }

        if (esMedico) {
            // Si es médico, solo actualizar el historial clínico
            Paciente pacienteExistente = pacienteService.getPacienteById(id);
            if (pacienteExistente != null) {
                pacienteExistente.setHistorialClinico(paciente.getHistorialClinico());
                pacienteService.updatePaciente(id, pacienteExistente);
            }
        } else {
            // Si no es médico, actualizar todos los datos
            pacienteService.updatePaciente(id, paciente);
        }

        return "redirect:/pacientes?exito=paciente_actualizado";
    }

    @PostMapping("/pacientes/eliminar/{id}")
    public String eliminarPaciente(@PathVariable("id") Long id) {
<<<<<<< HEAD
        boolean eliminado = pacienteService.deleteById(id);
        if (eliminado) {
            return "redirect:/pacientes?exito=paciente_eliminado";
        } else {
            return "redirect:/pacientes?error=no_se_puede_eliminar";
        }
=======
        pacienteService.deleteById(id);
        return "redirect:/pacientes";
>>>>>>> e3f9340d6092d67050804217941c637763468ac4
    }

}

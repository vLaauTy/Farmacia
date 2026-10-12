package Farmacia.Farmacia.controller;

import org.springframework.dao.DataAccessException;
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

// El ADMIN gestiona los pacientes; el MEDICO solo ve el listado y edita el historial clínico
// (ver UserConfig)
@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public String listarPacientes(Model model) {
        model.addAttribute("pacientes", pacienteService.getAllPacientes());
        return "pacientes";
    }

    @GetMapping("/nuevo")
    public String nuevoPaciente(Model model) {
        return mostrarFormulario(model, new Paciente(), false, null);
    }

    @PostMapping("/guardar")
    public String guardarPaciente(@ModelAttribute Paciente paciente, Model model) {
        if (pacienteService.existeOtroConDni(paciente.getDni(), null)) {
            return mostrarFormulario(model, paciente, false,
                    "Ya existe un paciente registrado con el DNI " + paciente.getDni() + ".");
        }

        pacienteService.savePaciente(paciente);
        return "redirect:/pacientes?exito=paciente_guardado";
    }

    @GetMapping("/editar/{id}")
    public String editarPaciente(@PathVariable Long id, Model model, Authentication authentication) {
        Paciente paciente = pacienteService.getPacienteById(id);
        if (paciente == null) {
            return "redirect:/pacientes?error=no_encontrado";
        }
        return mostrarFormulario(model, paciente, esMedico(authentication), null);
    }

    @PostMapping("/editar/{id}")
    public String actualizarPaciente(@PathVariable Long id, @ModelAttribute Paciente paciente,
            Authentication authentication, Model model) {
        if (esMedico(authentication)) {
            pacienteService.actualizarHistorial(id, paciente.getHistorialClinico());
            return "redirect:/pacientes?exito=paciente_actualizado";
        }

        paciente.setId(id);
        if (pacienteService.existeOtroConDni(paciente.getDni(), id)) {
            return mostrarFormulario(model, paciente, false,
                    "Ya existe otro paciente registrado con el DNI " + paciente.getDni() + ".");
        }

        pacienteService.updatePaciente(id, paciente);
        return "redirect:/pacientes?exito=paciente_actualizado";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarPaciente(@PathVariable Long id) {
        try {
            pacienteService.eliminar(id);
            return "redirect:/pacientes?exito=paciente_eliminado";
        } catch (DataAccessException e) {
            return "redirect:/pacientes?error=no_se_puede_eliminar";
        }
    }

    private String mostrarFormulario(Model model, Paciente paciente, boolean esMedico, String error) {
        model.addAttribute("paciente", paciente);
        model.addAttribute("esMedico", esMedico);
        model.addAttribute("error", error);
        return "formulario_paciente";
    }

    private static boolean esMedico(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_MEDICO"));
    }
}

package Farmacia.Farmacia.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import Farmacia.Farmacia.Model.Medico;
import Farmacia.Farmacia.Service.MedicoService;

// Alta, edición y baja de médicos (solo ADMIN, ver UserConfig)
@Controller
@RequestMapping("/medicos")
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @GetMapping
    public String listarMedicos(Model model) {
        model.addAttribute("medicos", medicoService.getAllMedicos());
        return "medicos";
    }

    @GetMapping("/nuevo")
    public String nuevoMedico(Model model) {
        return mostrarFormulario(model, new Medico(), null);
    }

    @PostMapping("/guardar")
    public String guardarMedico(@ModelAttribute Medico medico, Model model) {
        String error = validar(medico, null, null);
        if (error != null) {
            return mostrarFormulario(model, medico, error);
        }

        medicoService.registrar(medico);
        return "redirect:/medicos?exito=medico_guardado";
    }

    @GetMapping("/editar/{id}")
    public String editarMedico(@PathVariable Long id, Model model) {
        Medico medico = medicoService.getMedicoById(id);
        if (medico == null) {
            return "redirect:/medicos?error=medico_no_encontrado";
        }
        return mostrarFormulario(model, medico, null);
    }

    @PostMapping("/editar/{id}")
    public String actualizarMedico(@PathVariable Long id, @ModelAttribute Medico medico, Model model) {
        Medico original = medicoService.getMedicoById(id);
        if (original == null) {
            return "redirect:/medicos?error=medico_no_encontrado";
        }

        medico.setId(id);
        String error = validar(medico, id, original.getUsuario());
        if (error != null) {
            return mostrarFormulario(model, medico, error);
        }

        medicoService.actualizar(original, medico);
        return "redirect:/medicos?exito=medico_actualizado";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarMedico(@PathVariable Long id) {
        try {
            medicoService.eliminar(id);
            return "redirect:/medicos?eliminado";
        } catch (DataAccessException e) {
            return "redirect:/medicos?error=eliminacion";
        }
    }

    private String mostrarFormulario(Model model, Medico medico, String error) {
        model.addAttribute("medico", medico);
        model.addAttribute("error", error);
        return "formulario_medico";
    }

    // Devuelve el mensaje de error, o null si el DNI y el usuario están disponibles.
    // La vista resalta el campo según el mensaje contenga "DNI" o "usuario".
    private String validar(Medico medico, Long id, String usuarioActual) {
        if (medicoService.existeOtroConDni(medico.getDni(), id)) {
            return "Ya existe un médico registrado con el DNI " + medico.getDni() + ".";
        }
        if (medicoService.usuarioEnUso(medico.getUsuario(), usuarioActual)) {
            return "El nombre de usuario '" + medico.getUsuario() + "' ya está en uso.";
        }
        return null;
    }
}

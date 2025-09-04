package Farmacia.Farmacia.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/paciente/gestion")
    public String gestionPacientes(Model model) {
        return "gestion_pacientes";
    }

    @GetMapping("/pacientes")
    public String listarPacientes(Model model) {
        model.addAttribute("pacientes", pacienteService.getAllPacientes());
        return "pacientes";
    }

    @GetMapping("/pacientes/nuevo")
    public String mostrarFormularioNuevoPaciente(Model model) {
        Paciente paciente = new Paciente();
        model.addAttribute("paciente", paciente);
        return "formulario_paciente";
    }

    @PostMapping("/pacientes/guardar")
    public String guardarPaciente(@ModelAttribute("paciente") Paciente paciente) {
        pacienteService.savePaciente(paciente);
        return "redirect:/pacientes";
    }

    @GetMapping("/pacientes/editar/{id}")
    public String mostrarFormularioEditarPaciente(@PathVariable("id") Long id, Model model) {
        Paciente paciente = pacienteService.getPacienteById(id);
        model.addAttribute("paciente", paciente);
        return "formulario_paciente";
    }

    @PostMapping("/pacientes/editar/{id}")
    public String guardarEdicionPaciente(@PathVariable("id") Long id, @ModelAttribute("paciente") Paciente paciente) {
        pacienteService.updatePaciente(id, paciente);
        return "redirect:/pacientes";
    }

    @PostMapping("/pacientes/eliminar/{id}")
    public String eliminarPaciente(@PathVariable("id") Long id) {
        pacienteService.deleteById(id);
        return "redirect:/pacientes";
    }

}

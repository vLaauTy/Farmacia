package Farmacia.Farmacia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import Farmacia.Farmacia.Service.TurnoEmailService;

@Controller
public class CorreoPruebaController {

    private final TurnoEmailService turnoEmailService;

    public CorreoPruebaController(TurnoEmailService turnoEmailService) {
        this.turnoEmailService = turnoEmailService;
    }

    @GetMapping("/correo-prueba")
    public String mostrarFormulario() {
        return "correo_prueba";
    }

    @PostMapping("/correo-prueba")
    public String enviarCorreo(@RequestParam String destinatario, Model model) {
        String email = destinatario.trim();
        model.addAttribute("destinatario", email);

        if (turnoEmailService.enviarCorreoPrueba(email)) {
            model.addAttribute("mensaje", "Correo de prueba enviado correctamente.");
        } else {
            model.addAttribute("error", "No se pudo enviar. Revisá el destinatario y la configuración SMTP.");
        }
        return "correo_prueba";
    }
}
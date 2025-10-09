package Farmacia.Farmacia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import Farmacia.Farmacia.Service.MedicoService;
import Farmacia.Farmacia.Model.Medico;

@Controller
@RequestMapping
public class WebController {

    @Autowired
    private MedicoService medicoService;

    @GetMapping("/")
    public String menu(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();

            // Verificar si es ADMIN
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                model.addAttribute("rol", "ADMIN");
                model.addAttribute("username", username);
                return "menu_admin";
            }

            // Verificar si es MEDICO
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MEDICO"))) {
                // Buscar el médico en la tabla medico para obtener su nombre
                Medico medico = medicoService.getAllMedicos().stream()
                        .filter(m -> m.getUsuario().equals(username))
                        .findFirst()
                        .orElse(null);

                if (medico != null) {
                    model.addAttribute("rol", "MEDICO");
                    model.addAttribute("nombreCompleto", medico.getNombre() + " " + medico.getApellido());
                    model.addAttribute("username", username);
                    return "menu_medico";
                }
            }
        }

        // Si no está autenticado o no tiene rol válido, redirigir al login
        return "redirect:/login";
    }
}

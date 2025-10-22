package Farmacia.Farmacia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import Farmacia.Farmacia.Service.MedicoService;
import Farmacia.Farmacia.Service.TurnosService;
import Farmacia.Farmacia.Model.Medico;
import Farmacia.Farmacia.Model.Turnos;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping
public class WebController {

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private TurnosService turnosService;

    @GetMapping("/")
    public String menu(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();

            // Verificar si es ADMIN
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                // Obtener todos los turnos
                List<Turnos> todosTurnos = turnosService.obtenerTurnos();
                
                // Calcular especialidades con más turnos
                Map<String, Long> turnosPorEspecialidad = todosTurnos.stream()
                    .filter(turno -> turno.getMedico() != null && turno.getMedico().getEspecialidad() != null)
                    .collect(Collectors.groupingBy(
                        turno -> turno.getMedico().getEspecialidad(),
                        Collectors.counting()
                    ));
                
                // Obtener top 3 especialidades ordenadas por cantidad de turnos
                List<Map.Entry<String, Long>> top3Especialidades = turnosPorEspecialidad.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(3)
                    .collect(Collectors.toList());
                
                model.addAttribute("rol", "ADMIN");
                model.addAttribute("username", username);
                model.addAttribute("top3Especialidades", top3Especialidades);
                model.addAttribute("totalTurnos", todosTurnos.size());
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

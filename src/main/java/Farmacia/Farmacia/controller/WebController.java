package Farmacia.Farmacia.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import Farmacia.Farmacia.Model.Medico;
import Farmacia.Farmacia.Model.Turnos;
import Farmacia.Farmacia.Service.MedicoService;
import Farmacia.Farmacia.Service.PacienteService;
import Farmacia.Farmacia.Service.TurnosService;

// Página de inicio: muestra el panel que corresponde al rol del usuario
@Controller
public class WebController {

    private final MedicoService medicoService;
    private final TurnosService turnosService;
    private final PacienteService pacienteService;

    public WebController(MedicoService medicoService, TurnosService turnosService, PacienteService pacienteService) {
        this.medicoService = medicoService;
        this.turnosService = turnosService;
        this.pacienteService = pacienteService;
    }

    @GetMapping("/")
    public String inicio(Model model, Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return panelAdministrador(model);
        }

        Medico medico = medicoService.getMedicoByUsername(authentication.getName());
        if (medico != null) {
            model.addAttribute("nombreCompleto", medico.getNombre() + " " + medico.getApellido());
            return "menu_medico";
        }

        // Usuario sin un rol reconocido
        return "redirect:/login";
    }

    private String panelAdministrador(Model model) {
        List<Turnos> turnos = turnosService.obtenerTurnos();

        // Cantidad de turnos por especialidad, de mayor a menor (las 3 primeras)
        List<Map.Entry<String, Long>> top3Especialidades = turnos.stream()
                .filter(turno -> turno.getMedico() != null && turno.getMedico().getEspecialidad() != null)
                .collect(Collectors.groupingBy(turno -> turno.getMedico().getEspecialidad(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .toList();

        model.addAttribute("top3Especialidades", top3Especialidades);
        model.addAttribute("totalTurnos", turnos.size());
        model.addAttribute("turnosActivos", turnos.stream().filter(turno -> !turno.isCancelado()).count());
        model.addAttribute("totalMedicos", medicoService.getAllMedicos().size());
        model.addAttribute("totalPacientes", pacienteService.getAllPacientes().size());
        model.addAttribute("turnosCanceladosDelMes", turnosService.obtenerTurnosCanceladosDelMes());
        model.addAttribute("nombreMes", turnosService.obtenerNombreDelMes());
        return "menu_admin";
    }
}

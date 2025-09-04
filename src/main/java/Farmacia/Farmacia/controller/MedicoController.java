package Farmacia.Farmacia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import Farmacia.Farmacia.Model.Medico;
import Farmacia.Farmacia.Model.UserModel;
import Farmacia.Farmacia.Repository.UserRepository;
import Farmacia.Farmacia.Service.MedicoService;

@Controller
@RequestMapping
public class MedicoController {

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/medicos/gestion")
    public String gestionMedicos(Model model) {
        return "gestion_medicos";
    }

    @GetMapping("/medicos")
    public String listarMedicos(Model model) {
        model.addAttribute("medicos", medicoService.getAllMedicos());
        return "medicos";
    }

    @GetMapping("/medicos/nuevo")
    public String mostrarFormulario(Model model) {
        Medico medico = new Medico();
        // Inicializar valores por defecto
        medico.setTurnosCancelados(0);
        medico.setTurnosSemanales(0);
        // Forzar rol MEDICO siempre
        medico.setRol("MEDICO");
        model.addAttribute("medico", medico);
        return "formulario_medico";
    }

    @PostMapping("/medicos/guardar")
    public String guardarMedico(@ModelAttribute("medico") Medico medico) {
        // Asegurar que los valores no sean null
        if (medico.getTurnosCancelados() == null) {
            medico.setTurnosCancelados(0);
        }
        if (medico.getTurnosSemanales() == null) {
            medico.setTurnosSemanales(0);
        }

        // FORZAR que siempre sea MEDICO (seguridad adicional)
        medico.setRol("MEDICO");

        // Validar si ya existe un médico con el mismo DNI
        if (medicoService.existeMedicoPorDni(medico.getDni())) {
            System.out.println("⚠️ Médico ya registrado con ese DNI: " + medico.getDni());
            return "redirect:/medicos/nuevo?error=medico_ya_registrado";
        }

        try {
            // Guardar el médico
            medicoService.saveMedico(medico);

            // Crear usuario en tabla usuarios SIEMPRE con rol MEDICO
            // Verificar si el usuario ya existe
            if (userRepository.findByUsername(medico.getUsuario()).isEmpty()) {
                UserModel usuario = new UserModel();
                usuario.setUsername(medico.getUsuario());
                usuario.setPassword(passwordEncoder.encode(medico.getContraseña()));
                usuario.setRol("MEDICO"); // SIEMPRE MEDICO

                userRepository.save(usuario);
                System.out.println("✅ Médico y usuario creados: " + medico.getUsuario() + " con rol: MEDICO");
            } else {
                System.out.println("⚠️ Usuario ya existe: " + medico.getUsuario());
                return "redirect:/medicos/nuevo?error=usuario_existente";
            }

            return "redirect:/medicos?exito";
        } catch (Exception e) {
            System.err.println("❌ Error al guardar médico: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/medicos/nuevo?error=guardado";
        }
    }

    @GetMapping("/medicos/eliminar/{id}")
    public String eliminarMedico(@ModelAttribute("id") Long id) {
        try {
            // Obtener el médico antes de eliminarlo para eliminar también el usuario
            Medico medico = medicoService.getMedicoById(id);
            if (medico != null) {
                // Eliminar usuario asociado
                userRepository.findByUsername(medico.getUsuario()).ifPresent(userRepository::delete);
                System.out.println("🗑️ Usuario eliminado: " + medico.getUsuario());
            }

            // Eliminar médico
            medicoService.deleteMedico(id);
            System.out.println("🗑️ Médico eliminado: ID " + id);

            return "redirect:/medicos?eliminado";
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar médico: " + e.getMessage());
            return "redirect:/medicos?error=eliminacion";
        }
    }

    @GetMapping("/medicos/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Long id, Model model) {
        Medico medico = medicoService.getMedicoById(id);
        if (medico != null) {
            model.addAttribute("medico", medico);
            return "formulario_medico";
        }
        return "redirect:/medicos?error=medico_no_encontrado";
    }

    @PostMapping("/medicos/editar/{id}")
    public String editarMedico(@PathVariable("id") Long id, @ModelAttribute("medico") Medico medico) {
        // Asegurar que los valores no sean null
        if (medico.getTurnosCancelados() == null) {
            medico.setTurnosCancelados(0);
        }
        if (medico.getTurnosSemanales() == null) {
            medico.setTurnosSemanales(0);
        }

        // FORZAR que siempre sea MEDICO (seguridad adicional)
        medico.setRol("MEDICO");

        try {
            // Actualizar el médico
            medicoService.updateMedico(id, medico);
            return "redirect:/medicos?exito";
        } catch (Exception e) {
            System.err.println("Error al editar médico: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/medicos/editar/" + id + "?error=guardado";
        }
    }
}
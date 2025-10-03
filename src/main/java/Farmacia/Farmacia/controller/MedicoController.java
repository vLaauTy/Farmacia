package Farmacia.Farmacia.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
        // Si no hay médico en el modelo (primera carga), crear uno nuevo
        if (!model.containsAttribute("medico")) {
            model.addAttribute("medico", new Medico());
        }
        return "formulario_medico";
    }

    @PostMapping("/medicos/guardar")
    public String guardarMedico(@ModelAttribute("medico") Medico medico, Model model) {
        // Validar si ya existe un médico con el mismo DNI
        if (medicoService.existeMedicoPorDni(medico.getDni())) {
            model.addAttribute("medico", medico);
            model.addAttribute("error", "Ya existe un médico registrado con el DNI: " + medico.getDni());
            return "formulario_medico";
        }

        // Validar si ya existe un médico con el mismo usuario
        if (medicoService.existeMedicoPorUsuario(medico.getUsuario())) {
            model.addAttribute("medico", medico);
            model.addAttribute("error", "Ya existe un médico registrado con el usuario: " + medico.getUsuario());
            return "formulario_medico";
        }

        // validar si ya existe un usuario en la tabla usuarios
        if (userRepository.findByUsername(medico.getUsuario()).isPresent()) {
            model.addAttribute("medico", medico);
            model.addAttribute("error",
                    "El nombre de usuario '" + medico.getUsuario() + "' ya está en uso en el sistema");
            return "formulario_medico";
        }

        try {
            medicoService.saveMedico(medico);

            // Crear usuario en tabla usuarios
            UserModel usuario = new UserModel();
            usuario.setUsername(medico.getUsuario());
            usuario.setPassword(medico.getContraseña());
            usuario.setRol("MEDICO");
            userRepository.save(usuario);

            return "redirect:/medicos?exito=medico_guardado";
        } catch (Exception e) {
            model.addAttribute("medico", medico);
            model.addAttribute("error", "Error al guardar el médico. Por favor, intente nuevamente.");
            return "formulario_medico";
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
            }

            // Eliminar médico
            medicoService.deleteMedico(id);

            return "redirect:/medicos?eliminado";
        } catch (Exception e) {
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
    public String editarMedico(@PathVariable("id") Long id, @ModelAttribute("medico") Medico medico, Model model) {
        // Obtener el médico original una sola vez
        Medico medicoOriginal = medicoService.getMedicoById(id);
        if (medicoOriginal == null) {
            return "redirect:/medicos?error=medico_no_encontrado";
        }

        // Validar si ya existe otro médico con el mismo DNI
        if (medicoService.existeMedicoPorDniExcluyendoId(medico.getDni(), id)) {
            model.addAttribute("medico", medico);
            model.addAttribute("error", "Ya existe otro médico registrado con el DNI: " + medico.getDni());
            return "formulario_medico";
        }

        // Validar si ya existe otro médico con el mismo usuario
        if (medicoService.existeMedicoPorUsuarioExcluyendoId(medico.getUsuario(), id)) {
            model.addAttribute("medico", medico);
            model.addAttribute("error", "Ya existe otro médico registrado con el usuario: " + medico.getUsuario());
            return "formulario_medico";
        }

        // Validar si el usuario cambió y ya existe en el sistema (pero no es del médico
        // actual)
        if (!medicoOriginal.getUsuario().equals(medico.getUsuario())) {
            if (userRepository.findByUsername(medico.getUsuario()).isPresent()) {
                model.addAttribute("medico", medico);
                model.addAttribute("error",
                        "El nombre de usuario '" + medico.getUsuario() + "' ya está en uso en el sistema");
                return "formulario_medico";
            }
        }

        try {
            // Si la contraseña está vacía, mantener la original
            if (medico.getContraseña() == null || medico.getContraseña().trim().isEmpty()) {
                medico.setContraseña(medicoOriginal.getContraseña());
            }

            // Actualizar médico
            medicoService.updateMedico(id, medico);

            // Actualizar usuario en tabla usuarios si cambió el usuario o contraseña
            userRepository.findByUsername(medicoOriginal.getUsuario()).ifPresent(usuario -> {
                if (!medicoOriginal.getUsuario().equals(medico.getUsuario())) {
                    // Si cambió el usuario, eliminar el anterior y crear uno nuevo
                    userRepository.delete(usuario);
                    UserModel nuevoUsuario = new UserModel();
                    nuevoUsuario.setUsername(medico.getUsuario());
                    nuevoUsuario.setPassword(medico.getContraseña());
                    nuevoUsuario.setRol("MEDICO");
                    userRepository.save(nuevoUsuario);
                } else if (medico.getContraseña() != null && !medico.getContraseña().trim().isEmpty()
                        && !medico.getContraseña().equals(usuario.getPassword())) {
                    // Si cambió la contraseña, actualizarla (solo si no está vacía y es diferente)
                    usuario.setPassword(medico.getContraseña());
                    userRepository.save(usuario);
                }
            });

            return "redirect:/medicos?exito=medico_actualizado";
        } catch (Exception e) {
            model.addAttribute("medico", medico);
            model.addAttribute("error", "Error al actualizar el médico. Por favor, intente nuevamente.");
            return "formulario_medico";
        }
    }
}
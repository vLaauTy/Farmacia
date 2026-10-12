package Farmacia.Farmacia.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import Farmacia.Farmacia.Repository.UserRepository;

@Controller
@RequestMapping("/usuarios")
public class UsuariosController {

    private final UserRepository userRepository;

    public UsuariosController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listarUsuarios(Model model) {
        List<Map<String, String>> usuarios = userRepository.findAll().stream()
                .map(usuario -> Map.of(
                        "username", usuario.getUsername(),
                        "rol", usuario.getRol()))
                .toList();
        model.addAttribute("usuarios", usuarios);
        return "usuarios";
    }
}
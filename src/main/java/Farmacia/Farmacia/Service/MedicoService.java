package Farmacia.Farmacia.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Farmacia.Farmacia.Model.Medico;
import Farmacia.Farmacia.Model.UserModel;
import Farmacia.Farmacia.Repository.MedicoRepository;
import Farmacia.Farmacia.Repository.UserRepository;

@Service
public class MedicoService {

    private final MedicoRepository repo;
    private final UserRepository userRepository;

    public MedicoService(MedicoRepository repo, UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    public List<Medico> getAllMedicos() {
        return repo.findAll();
    }

    public Medico getMedicoById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Medico getMedicoByUsername(String username) {
        return repo.findByUsuario(username).orElse(null);
    }

    // Indica si otro médico (distinto de idExcluido) ya usa ese DNI
    public boolean existeOtroConDni(String dni, Long idExcluido) {
        return repo.findByDni(dni)
                .map(medico -> !medico.getId().equals(idExcluido))
                .orElse(false);
    }

    // Indica si el nombre de usuario ya pertenece a otra cuenta (médico o administrador)
    public boolean usuarioEnUso(String usuario, String usuarioActual) {
        if (usuario.equals(usuarioActual)) {
            return false;
        }
        return repo.existsByUsuario(usuario) || userRepository.existsByUsername(usuario);
    }

    // Guarda el médico y crea su usuario para iniciar sesión
    @Transactional
    public void registrar(Medico medico) {
        repo.save(medico);

        UserModel usuario = new UserModel();
        usuario.setUsername(medico.getUsuario());
        usuario.setPassword(medico.getContraseña());
        usuario.setRol("MEDICO");
        userRepository.save(usuario);
    }

    // Actualiza el médico y mantiene sincronizado su usuario de acceso
    @Transactional
    public void actualizar(Medico original, Medico medico) {
        medico.setId(original.getId());
        if (medico.getContraseña() == null || medico.getContraseña().isBlank()) {
            medico.setContraseña(original.getContraseña());
        }
        repo.save(medico);

        UserModel usuario = userRepository.findByUsername(original.getUsuario()).orElseGet(UserModel::new);
        usuario.setUsername(medico.getUsuario());
        usuario.setPassword(medico.getContraseña());
        usuario.setRol("MEDICO");
        userRepository.save(usuario);
    }

    // Elimina el médico junto con su usuario. Falla si el médico tiene turnos asignados.
    @Transactional
    public void eliminar(Long id) {
        repo.findById(id).ifPresent(medico -> {
            userRepository.findByUsername(medico.getUsuario()).ifPresent(userRepository::delete);
            repo.delete(medico);
            repo.flush(); // fuerza la verificación de turnos asociados acá y no al confirmar
        });
    }
}

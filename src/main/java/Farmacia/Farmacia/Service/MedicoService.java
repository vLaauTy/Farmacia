
package Farmacia.Farmacia.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import Farmacia.Farmacia.Model.Medico;
import Farmacia.Farmacia.Repository.MedicoRepository;

@Service
public class MedicoService {
    private final MedicoRepository repo;

    public MedicoService(MedicoRepository repo) {
        this.repo = repo;
    }

    public List<Medico> getAllMedicos() {
        return repo.findAll();
    }

    public Medico getMedicoById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Medico saveMedico(Medico medico) {
        return repo.save(medico);
    }

    public void deleteMedico(Long id) {
        repo.deleteById(id);
    }

    public Medico updateMedico(Long id, Medico medico) {
        medico.setId(id);
        return repo.save(medico);
    }

    public List<Medico> findByEspecialidad(String especialidad) {
        return repo.findByEspecialidad(especialidad);
    }

    public List<Medico> findByNombre(String nombre) {
        return repo.findByNombreIgnoreCase(nombre);
    }

    // Verifica si existe un médico con el mismo DNI
    public boolean existeMedicoPorDni(String dni) {
        return repo.existsByDni(dni);
    }

    // Verifica si existe un médico con el mismo DNI excluyendo un ID específico
    public boolean existeMedicoPorDniExcluyendoId(String dni, Long id) {
        return repo.findOptionalByDni(dni)
                .map(medico -> !medico.getId().equals(id))
                .orElse(false);
    }

    // Verifica si existe un médico con el mismo usuario
    public boolean existeMedicoPorUsuario(String usuario) {
        return repo.existsByUsuario(usuario);
    }

    // Verifica si existe un médico con el mismo usuario excluyendo un ID específico
    public boolean existeMedicoPorUsuarioExcluyendoId(String usuario, Long id) {
        return repo.findByUsuario(usuario)
                .map(medico -> !medico.getId().equals(id))
                .orElse(false);
    }
}

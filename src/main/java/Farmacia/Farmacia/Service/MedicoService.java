
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
        if (repo.existsById(id)) {
            medico.setId(id);
            return repo.save(medico);
        }
        return null;
    }

    public List<Medico> findByEspecialidad(String especialidad) {
        return repo.findByEspecialidad(especialidad);
    }

    public List<Medico> findByNombre(String nombre) {
        return repo.findAll().stream()
                .filter(medico -> medico.getNombre().equalsIgnoreCase(nombre))
                .toList();
    }

    // Verifica si existe un médico con el mismo DNI
    public boolean existeMedicoPorDni(String dni) {
        return repo.findByDni(dni) != null;
    }
}

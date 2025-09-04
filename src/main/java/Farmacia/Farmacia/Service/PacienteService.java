package Farmacia.Farmacia.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import Farmacia.Farmacia.Model.Paciente;
import Farmacia.Farmacia.Repository.PacienteRepository;

@Service
public class PacienteService {
    private final PacienteRepository repo;

    public PacienteService(PacienteRepository repo) {
        this.repo = repo;
    }

    public List<Paciente> getAllPacientes() {
        return repo.findAll();
    }

    public Paciente getPacienteById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Paciente savePaciente(Paciente paciente) {
        return repo.save(paciente);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public Paciente updatePaciente(Long id, Paciente paciente) {
        if (repo.existsById(id)) {
            paciente.setId(id);
            return repo.save(paciente);
        }
        return null;
    }

    public List<Paciente> findByNombre(String nombre) {
        return repo.findAll().stream()
                .filter(paciente -> paciente.getNombre().equalsIgnoreCase(nombre))
                .toList();
    }

}

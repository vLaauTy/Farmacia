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
        return repo.findAll().stream()
                .filter(p -> p.getId() != null && p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Paciente savePaciente(Paciente paciente) {
        return repo.save(paciente);
    }

    public void deleteById(Long id) {
        // Para implementación temporal sin JPA
        // Podríamos implementar lógica de eliminación si fuera necesario
    }

    public Paciente updatePaciente(Long id, Paciente paciente) {
        paciente.setId(id);
        return repo.save(paciente);
    }

    public List<Paciente> findByNombre(String nombre) {
        return repo.findAll().stream()
                .filter(paciente -> paciente.getNombre().equalsIgnoreCase(nombre))
                .toList();
    }

    public boolean existePacientePorDni(String dni) {
        return repo.existsByDni(dni);
    }

    public boolean existePacientePorDniExcluyendoId(String dni, Long id) {
        return repo.findByDni(dni)
                .map(paciente -> !paciente.getId().equals(id))
                .orElse(false);
    }

}

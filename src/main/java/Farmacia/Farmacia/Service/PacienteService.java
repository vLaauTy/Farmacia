package Farmacia.Farmacia.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public void savePaciente(Paciente paciente) {
        repo.save(paciente);
    }

    public void updatePaciente(Long id, Paciente paciente) {
        paciente.setId(id);
        repo.save(paciente);
    }

    // El médico solo puede modificar el historial clínico
    public void actualizarHistorial(Long id, String historialClinico) {
        repo.findById(id).ifPresent(paciente -> {
            paciente.setHistorialClinico(historialClinico);
            repo.save(paciente);
        });
    }

    // Indica si otro paciente (distinto de idExcluido) ya usa ese DNI
    public boolean existeOtroConDni(String dni, Long idExcluido) {
        return repo.findByDni(dni)
                .map(paciente -> !paciente.getId().equals(idExcluido))
                .orElse(false);
    }

    // Falla si el paciente tiene turnos asignados
    @Transactional
    public void eliminar(Long id) {
        repo.deleteById(id);
        repo.flush(); // fuerza la verificación de turnos asociados acá y no al confirmar
    }
}

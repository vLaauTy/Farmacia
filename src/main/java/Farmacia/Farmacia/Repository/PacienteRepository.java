package Farmacia.Farmacia.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Farmacia.Farmacia.Model.Paciente;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    
    Optional<Paciente> findByDni(String dni);
    
    boolean existsByDni(String dni);
}

package Farmacia.Farmacia.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Farmacia.Farmacia.Model.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByDni(String dni);
}

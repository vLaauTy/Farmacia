package Farmacia.Farmacia.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Farmacia.Farmacia.Model.Medico;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    Optional<Medico> findByDni(String dni);

    Optional<Medico> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);
}

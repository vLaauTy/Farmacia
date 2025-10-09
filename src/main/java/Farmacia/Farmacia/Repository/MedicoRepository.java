package Farmacia.Farmacia.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Farmacia.Farmacia.Model.Medico;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    List<Medico> findByEspecialidad(String especialidad);

    List<Medico> findByNombreIgnoreCase(String nombre);

    Medico findByDni(String dni);

    Optional<Medico> findOptionalByDni(String dni);

    boolean existsByDni(String dni);

    Optional<Medico> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);
}

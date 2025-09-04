package Farmacia.Farmacia.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Farmacia.Farmacia.Model.Medico;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    List<Medico> findByEspecialidad(String especialidad);

    // Buscar médico por DNI
    Medico findByDni(String dni);
}

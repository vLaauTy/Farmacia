package Farmacia.Farmacia.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Farmacia.Farmacia.Model.Turnos;

public interface TurnosRepository extends JpaRepository<Turnos, Long> {

    List<Turnos> findByMedicoId(Long medicoId);
}

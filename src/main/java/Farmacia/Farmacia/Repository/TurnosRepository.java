package Farmacia.Farmacia.Repository;

import Farmacia.Farmacia.Model.Turnos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TurnosRepository extends JpaRepository<Turnos, Long> {

}

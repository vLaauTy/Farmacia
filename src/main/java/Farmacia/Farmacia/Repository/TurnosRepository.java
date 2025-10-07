package Farmacia.Farmacia.Repository;

import Farmacia.Farmacia.Model.Turnos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TurnosRepository extends JpaRepository<Turnos, Long> {

    // MÉTODOS ESENCIALES: Para funcionamiento básico de turnos
    
    // Buscar turnos por médico y fecha 
    List<Turnos> findByMedicoIdAndDiaAndMesAndAnio(Long medicoId, Integer dia, Integer mes, Integer anio);
    
    // Verificar duplicados (evita turnos en mismo horario)
    boolean existsByMedicoIdAndDiaAndMesAndAnioAndHora(Long medicoId, Integer dia, Integer mes, Integer anio, String hora);
}

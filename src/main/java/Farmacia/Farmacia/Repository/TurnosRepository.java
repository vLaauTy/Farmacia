package Farmacia.Farmacia.Repository;

import Farmacia.Farmacia.Model.Turnos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TurnosRepository extends JpaRepository<Turnos, Long> {

        // Obtener todos los turnos de un médico ordenados por fecha y hora (ascendente)
        List<Turnos> findByMedicoIdOrderByAnioAscMesAscDiaAscHoraAsc(Long medicoId);

        // Verificar si un paciente ya tiene un turno en una fecha y hora específica
        boolean existsByPacienteIdAndDiaAndMesAndAnioAndHora(Long pacienteId, Integer dia, Integer mes, Integer anio,
                        String hora);
}

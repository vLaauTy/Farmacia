package Farmacia.Farmacia.Repository;

import Farmacia.Farmacia.Model.Turnos;
import Farmacia.Farmacia.Model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TurnosRepository extends JpaRepository<Turnos, Long> {

    // Buscar turnos por médico, día, mes y año usando métodos automáticos de Spring
    List<Turnos> findByMedicoAndDiaAndMesAndAnio(Medico medico, Integer dia, Integer mes, Integer anio);

    // Verificar si existe un turno específico para evitar duplicados
    boolean existsByMedicoAndDiaAndMesAndAnioAndHora(Medico medico, Integer dia, Integer mes, Integer anio,
            String hora);

    // Verificar existencia usando ID del médico (más eficiente)
    boolean existsByMedicoIdAndDiaAndMesAndAnioAndHora(Long medicoId, Integer dia, Integer mes, Integer anio,
            String hora);

    // Obtener todos los turnos de un médico ordenados por fecha y hora
    List<Turnos> findByMedicoIdOrderByAnioAscMesAscDiaAscHoraAsc(Long medicoId);

    // Verificar si un paciente ya tiene un turno en una fecha y hora específica
    boolean existsByPacienteIdAndDiaAndMesAndAnioAndHora(Long pacienteId, Integer dia, Integer mes, Integer anio,
            String hora);
}

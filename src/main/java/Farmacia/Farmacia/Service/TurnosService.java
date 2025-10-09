package Farmacia.Farmacia.Service;

import Farmacia.Farmacia.Model.Turnos;
import Farmacia.Farmacia.Repository.TurnosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class TurnosService {

    @Autowired
    TurnosRepository turnosRepository;

    public ArrayList<Turnos> obtenerTurnos() {
        return (ArrayList<Turnos>) turnosRepository.findAll();
    }

    public Turnos guardarTurno(Turnos turno) {
        return turnosRepository.save(turno);
    }

    public Optional<Turnos> obtenerPorId(Long id) {
        return turnosRepository.findById(id);
    }

    public boolean eliminarTurno(Long id) {
        try {
            turnosRepository.deleteById(id);
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    // Método para verificar si existe un turno en una fecha y hora específica
    public boolean existeTurnoEnFechaYHora(Long medicoId, Integer dia, Integer mes, Integer anio, String hora) {
        return turnosRepository.existsByMedicoIdAndDiaAndMesAndAnioAndHora(medicoId, dia, mes, anio, hora);
    }

    // Método para obtener turnos de un médico específico
    public ArrayList<Turnos> obtenerTurnosPorMedico(Long medicoId) {
        return (ArrayList<Turnos>) turnosRepository.findByMedicoIdOrderByAnioAscMesAscDiaAscHoraAsc(medicoId);
    }

    // Método para verificar si un paciente ya tiene un turno en una fecha y hora específica
    public boolean existeTurnoPaciente(Long pacienteId, Integer dia, Integer mes, Integer anio, String hora) {
        return turnosRepository.existsByPacienteIdAndDiaAndMesAndAnioAndHora(pacienteId, dia, mes, anio, hora);
    }

    // Método para verificar turnos del paciente excluyendo un ID específico (para edición)
    public boolean existeTurnoPacienteExcluyendoId(Long pacienteId, Integer dia, Integer mes, Integer anio, String hora, Long turnoId) {
        // Buscar todos los turnos que coincidan con paciente, fecha y hora
        return obtenerTurnos().stream()
                .anyMatch(turno -> 
                    turno.getPaciente() != null &&
                    turno.getPaciente().getId().equals(pacienteId) &&
                    turno.getDia().equals(dia) &&
                    turno.getMes().equals(mes) &&
                    turno.getAnio().equals(anio) &&
                    turno.getHora().equals(hora) &&
                    !turno.getId().equals(turnoId) // Excluir el turno actual
                );
    }

    // Método para obtener turnos entre fechas específicas
    public ArrayList<Turnos> obtenerTurnosEntreFechas(java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {
        return (ArrayList<Turnos>) obtenerTurnos().stream()
                .filter(turno -> {
                    java.time.LocalDate fechaTurno = java.time.LocalDate.of(turno.getAnio(), turno.getMes(), turno.getDia());
                    return !fechaTurno.isBefore(fechaInicio) && !fechaTurno.isAfter(fechaFin);
                })
                .collect(java.util.stream.Collectors.toList());
    }

}

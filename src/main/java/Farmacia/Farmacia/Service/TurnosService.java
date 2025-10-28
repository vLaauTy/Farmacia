package Farmacia.Farmacia.Service;

import Farmacia.Farmacia.Model.Turnos;
import Farmacia.Farmacia.Repository.TurnosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

@Service
public class TurnosService {

    @Autowired
    TurnosRepository turnosRepository;

    public ArrayList<Turnos> obtenerTurnos() {
        return (ArrayList<Turnos>) turnosRepository.findAll();
    }

    // Método para obtener solo turnos activos (no cancelados)
    public ArrayList<Turnos> obtenerTurnosActivos() {
    ArrayList<Turnos> activos = (ArrayList<Turnos>) obtenerTurnos().stream()
        .filter(turno -> !"CANCELADO".equals(turno.getEstado()))
        .collect(java.util.stream.Collectors.toList());

    // Ordenar por fecha y hora (más recientes primero)
    activos.sort(Comparator
        .comparing(Turnos::getAnio, Comparator.reverseOrder())
        .thenComparing(Turnos::getMes, Comparator.reverseOrder())
        .thenComparing(Turnos::getDia, Comparator.reverseOrder())
        .thenComparing(t -> LocalTime.parse(t.getHora()), Comparator.reverseOrder()));

    return activos;
    }

    // Método para obtener turnos cancelados
    public ArrayList<Turnos> obtenerTurnosCancelados() {
        return (ArrayList<Turnos>) obtenerTurnos().stream()
                .filter(turno -> "CANCELADO".equals(turno.getEstado()))
                .collect(java.util.stream.Collectors.toList());
    }

    // Método para obtener turnos cancelados del mes actual
    public long obtenerTurnosCanceladosDelMes() {
        LocalDate hoy = LocalDate.now();
        int mesActual = hoy.getMonthValue();
        int anioActual = hoy.getYear();
        
        return obtenerTurnos().stream()
                .filter(turno -> "CANCELADO".equals(turno.getEstado()))
                .filter(turno -> turno.getMes() == mesActual && turno.getAnio() == anioActual)
                .count();
    }

    // Método para obtener nombre del mes actual
    public String obtenerNombreDelMes() {
        LocalDate hoy = LocalDate.now();
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[hoy.getMonthValue() - 1];
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

    // Método para cancelar un turno (marcarlo como cancelado en lugar de eliminarlo)
    public boolean cancelarTurno(Long id) {
        try {
            Optional<Turnos> turnoOptional = turnosRepository.findById(id);
            if (turnoOptional.isPresent()) {
                Turnos turno = turnoOptional.get();
                turno.setEstado("CANCELADO");
                turnosRepository.save(turno);
                return true;
            }
            return false;
        } catch (Exception err) {
            return false;
        }
    }

    // Método para verificar si existe un turno en una fecha y hora específica
    public boolean existeTurnoEnFechaYHora(Long medicoId, Integer dia, Integer mes, Integer anio, String hora) {
        return turnosRepository.existsByMedicoIdAndDiaAndMesAndAnioAndHora(medicoId, dia, mes, anio, hora);
    }

    // Método para obtener turnos de un médico específico (ordenados por fecha más reciente primero)
    public ArrayList<Turnos> obtenerTurnosPorMedico(Long medicoId) {
        // Obtener todos los turnos del médico sin ordenamiento específico
        ArrayList<Turnos> turnos = (ArrayList<Turnos>) turnosRepository.findByMedicoIdOrderByAnioAscMesAscDiaAscHoraAsc(medicoId);
        
        // Ordenar manualmente por fecha y hora (más reciente primero)
        turnos.sort(Comparator
            .comparing(Turnos::getAnio, Comparator.reverseOrder()) // Año descendente (2025, 2024, 2023...)
            .thenComparing(Turnos::getMes, Comparator.reverseOrder()) // Mes descendente (12, 11, 10...)
            .thenComparing(Turnos::getDia, Comparator.reverseOrder()) // Día descendente (31, 30, 29...)
            .thenComparing(turno -> LocalTime.parse(turno.getHora()), Comparator.reverseOrder()) // Hora descendente (18:00, 17:00, 16:00...)
        );
        
        return turnos;
    }

    // Método para verificar si un paciente ya tiene un turno en una fecha y hora
    // específica
    public boolean existeTurnoPaciente(Long pacienteId, Integer dia, Integer mes, Integer anio, String hora) {
        return turnosRepository.existsByPacienteIdAndDiaAndMesAndAnioAndHora(pacienteId, dia, mes, anio, hora);
    }

    // Método para verificar turnos del paciente excluyendo un ID específico (para
    // edición)
    public boolean existeTurnoPacienteExcluyendoId(Long pacienteId, Integer dia, Integer mes, Integer anio, String hora,
            Long turnoId) {
        // Buscar todos los turnos que coincidan con paciente, fecha y hora
        return obtenerTurnos().stream()
                .anyMatch(turno -> turno.getPaciente() != null &&
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
                    java.time.LocalDate fechaTurno = java.time.LocalDate.of(turno.getAnio(), turno.getMes(),
                            turno.getDia());
                    return !fechaTurno.isBefore(fechaInicio) && !fechaTurno.isAfter(fechaFin);
                })
                .collect(java.util.stream.Collectors.toList());
    }

}

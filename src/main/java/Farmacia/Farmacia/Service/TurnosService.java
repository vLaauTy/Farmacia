package Farmacia.Farmacia.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import Farmacia.Farmacia.Model.Medico;
import Farmacia.Farmacia.Model.Turnos;
import Farmacia.Farmacia.Repository.TurnosRepository;

@Service
public class TurnosService {

    // Del más antiguo al más reciente
    private static final Comparator<Turnos> POR_FECHA_Y_HORA = Comparator
            .comparing(Turnos::getFecha)
            .thenComparing(turno -> LocalTime.parse(turno.getHora()));

    private static final String[] MESES = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" };

    private final TurnosRepository turnosRepository;

    public TurnosService(TurnosRepository turnosRepository) {
        this.turnosRepository = turnosRepository;
    }

    public List<Turnos> obtenerTurnos() {
        return turnosRepository.findAll();
    }

    public Optional<Turnos> obtenerPorId(Long id) {
        return turnosRepository.findById(id);
    }

    public void guardarTurno(Turnos turno) {
        turnosRepository.save(turno);
    }

    // Los turnos no se borran: se marcan como cancelados para llevar la estadística
    public void cancelarTurno(Long id) {
        turnosRepository.findById(id).ifPresent(turno -> {
            turno.setEstado(Turnos.CANCELADO);
            turnosRepository.save(turno);
        });
    }

    // Turnos no cancelados, del más reciente al más antiguo
    public List<Turnos> obtenerTurnosActivos() {
        return activos(turnosRepository.findAll().stream())
                .sorted(POR_FECHA_Y_HORA.reversed())
                .toList();
    }

    // Turnos no cancelados de un médico, del más reciente al más antiguo
    public List<Turnos> obtenerTurnosPorMedico(Long medicoId) {
        return activos(turnosRepository.findByMedicoId(medicoId).stream())
                .sorted(POR_FECHA_Y_HORA.reversed())
                .toList();
    }

    // Turnos no cancelados entre dos fechas (ambas incluidas)
    public List<Turnos> obtenerTurnosEntreFechas(LocalDate desde, LocalDate hasta) {
        return activos(turnosRepository.findAll().stream())
                .filter(turno -> !turno.getFecha().isBefore(desde) && !turno.getFecha().isAfter(hasta))
                .toList();
    }

    public long obtenerTurnosCanceladosDelMes() {
        YearMonth mesActual = YearMonth.now();
        return turnosRepository.findAll().stream()
                .filter(Turnos::isCancelado)
                .filter(turno -> YearMonth.from(turno.getFecha()).equals(mesActual))
                .count();
    }

    public String obtenerNombreDelMes() {
        return MESES[LocalDate.now().getMonthValue() - 1];
    }

    // ---------- Disponibilidad ----------

    public boolean medicoOcupado(Long medicoId, LocalDate fecha, String hora, Long turnoExcluido) {
        return existeTurnoActivo(turno -> turno.getMedico() != null && turno.getMedico().getId().equals(medicoId),
                fecha, hora, turnoExcluido);
    }

    public boolean pacienteOcupado(Long pacienteId, LocalDate fecha, String hora, Long turnoExcluido) {
        return existeTurnoActivo(turno -> turno.getPaciente() != null && turno.getPaciente().getId().equals(pacienteId),
                fecha, hora, turnoExcluido);
    }

    // Busca un turno activo en la misma fecha y hora que cumpla la condición,
    // ignorando el turno que se está editando
    private boolean existeTurnoActivo(Predicate<Turnos> condicion, LocalDate fecha, String hora, Long turnoExcluido) {
        return activos(turnosRepository.findAll().stream())
                .filter(turno -> !turno.getId().equals(turnoExcluido))
                .filter(turno -> turno.getFecha().equals(fecha) && turno.getHora().equals(hora))
                .anyMatch(condicion);
    }

    // ---------- Horarios ----------

    // Horarios cada 30 minutos según el turno de atención del médico
    public List<String> horariosDelMedico(Medico medico) {
        return switch (medico.getHorario()) {
            case "Mañana (8:00 - 14:00)" -> generarHorarios(LocalTime.of(8, 0), LocalTime.of(14, 0));
            case "Tarde (14:00 - 20:00)" -> generarHorarios(LocalTime.of(14, 0), LocalTime.of(20, 0));
            case "Tiempo Completo (8:00 - 20:00)" -> generarHorarios(LocalTime.of(8, 0), LocalTime.of(20, 0));
            default -> List.of();
        };
    }

    // Horarios cada 30 minutos entre inicio (incluido) y fin (excluido), en formato HH:mm
    public List<String> generarHorarios(LocalTime inicio, LocalTime fin) {
        return Stream.iterate(inicio, hora -> hora.isBefore(fin), hora -> hora.plus(Duration.ofMinutes(30)))
                .map(hora -> hora.format(DateTimeFormatter.ofPattern("HH:mm")))
                .toList();
    }

    private static Stream<Turnos> activos(Stream<Turnos> turnos) {
        return turnos.filter(turno -> !turno.isCancelado());
    }
}

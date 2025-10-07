package Farmacia.Farmacia.Service;

import Farmacia.Farmacia.Model.Turnos;
import Farmacia.Farmacia.Repository.TurnosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    // Buscar turnos por médico y fecha específica (usado en validaciones)
    public List<Turnos> obtenerTurnosPorMedicoYFecha(Long medicoId, Integer dia, Integer mes, Integer anio) {
        return turnosRepository.findByMedicoIdAndDiaAndMesAndAnio(medicoId, dia, mes, anio);
    }
}

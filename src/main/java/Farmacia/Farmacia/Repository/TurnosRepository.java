package Farmacia.Farmacia.Repository;

import Farmacia.Farmacia.Model.Turnos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TurnosRepository extends JpaRepository<Turnos, Long> {

    // Buscar turnos por médico, día, mes y año
    @Query("SELECT t FROM Turnos t WHERE t.medico.id = :medicoId AND t.dia = :dia AND t.mes = :mes AND t.anio = :anio")
    List<Turnos> findByMedicoAndFecha(@Param("medicoId") Long medicoId, 
                                      @Param("dia") Integer dia, 
                                      @Param("mes") Integer mes, 
                                      @Param("anio") Integer anio);

    // Verificar si existe un turno específico para evitar duplicados
    @Query("SELECT COUNT(t) > 0 FROM Turnos t WHERE t.medico.id = :medicoId AND t.dia = :dia AND t.mes = :mes AND t.anio = :anio AND t.hora = :hora")
    boolean existsByMedicoAndFechaAndHora(@Param("medicoId") Long medicoId, 
                                         @Param("dia") Integer dia, 
                                         @Param("mes") Integer mes, 
                                         @Param("anio") Integer anio, 
                                         @Param("hora") String hora);
}

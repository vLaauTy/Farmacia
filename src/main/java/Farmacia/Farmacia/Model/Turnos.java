package Farmacia.Farmacia.Model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "turnos")
public class Turnos {

    public static final String ACTIVO = "ACTIVO";
    public static final String CANCELADO = "CANCELADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacion ManyToOne que indica que muchos turnos pueden estar asociados a un
    // solo médico o paciente
    @ManyToOne
    @JoinColumn(name = "medico_id", foreignKey = @ForeignKey(name = "FK_turnos_medico"))
    private Medico medico;

    @ManyToOne
    @JoinColumn(name = "paciente_id", foreignKey = @ForeignKey(name = "FK_turnos_paciente"))
    private Paciente paciente;

    @Column
    private Integer dia;

    @Column
    private Integer mes;

    @Column
    private Integer anio;

    @Column
    private String hora;

    @Column
    private String estado = ACTIVO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Integer getDia() {
        return dia;
    }

    public void setDia(Integer dia) {
        this.dia = dia;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isCancelado() {
        return CANCELADO.equals(estado);
    }

    // Fecha del turno armada a partir de día, mes y año (null si falta alguno)
    public LocalDate getFecha() {
        if (anio == null || mes == null || dia == null) {
            return null;
        }
        return LocalDate.of(anio, mes, dia);
    }
}

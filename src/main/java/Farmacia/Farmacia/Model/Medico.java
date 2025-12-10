package Farmacia.Farmacia.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "medico")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String usuario;

    @Column(nullable = false)
    private String contraseña;

    @Column(nullable = false)
    private String rol = "MEDICO";

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String dni;

    @Column(name = "domicilio")
    private String domicilio;

    @Column
    private String telefono;

    @Column
    private String especialidad;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer turnosCancelados = 0;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer turnosSemanales = 0;

    @Column
    private String horario;

    // Constructor por defecto
    public Medico() {
        this.rol = "MEDICO";
        this.turnosCancelados = 0;
        this.turnosSemanales = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        // Siempre forzar que sea MEDICO por seguridad
        this.rol = "MEDICO";
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public Integer getTurnosCancelados() {
        return turnosCancelados;
    }

    public void setTurnosCancelados(Integer turnosCancelados) {
        this.turnosCancelados = turnosCancelados;
    }

    public Integer getTurnosSemanales() {
        return turnosSemanales;
    }

    public void setTurnosSemanales(Integer turnosSemanales) {
        this.turnosSemanales = turnosSemanales;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

}

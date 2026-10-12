package Farmacia.Farmacia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import Farmacia.Farmacia.Model.Medico;
import Farmacia.Farmacia.Model.Paciente;
import Farmacia.Farmacia.Model.Turnos;
import Farmacia.Farmacia.Model.UserModel;
import Farmacia.Farmacia.Repository.MedicoRepository;
import Farmacia.Farmacia.Repository.PacienteRepository;
import Farmacia.Farmacia.Repository.TurnosRepository;
import Farmacia.Farmacia.Repository.UserRepository;

// Pruebas de punta a punta: levantan la aplicación completa con una base en memoria
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SaludSyncApplicationTests {

    private static final RequestPostProcessor ADMIN = user("admin").roles("ADMIN");
    private static final RequestPostProcessor MEDICO = user("jperez").roles("MEDICO");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MedicoRepository medicos;

    @Autowired
    private PacienteRepository pacientes;

    @Autowired
    private TurnosRepository turnos;

    @Autowired
    private UserRepository usuarios;

    private Medico medico;
    private Paciente paciente;

    // Cada test arranca con un médico (con su usuario) y un paciente
    @BeforeEach
    void prepararDatos() {
        turnos.deleteAll();
        pacientes.deleteAll();
        medicos.deleteAll();
        usuarios.deleteAll();

        medico = new Medico();
        medico.setNombre("Juan");
        medico.setApellido("Pérez");
        medico.setDni("30111222");
        medico.setTelefono("11-1111-1111");
        medico.setDomicilio("Calle 1");
        medico.setEspecialidad("Cardiología");
        medico.setHorario("Mañana (8:00 - 14:00)");
        medico.setUsuario("jperez");
        medico.setContraseña("clave123");
        medico = medicos.save(medico);
        usuarios.save(usuario("jperez", "clave123", "MEDICO"));

        paciente = new Paciente();
        paciente.setNombre("María");
        paciente.setApellido("García");
        paciente.setDni("40111222");
        paciente.setTelefono("11-2222-2222");
        paciente.setDomicilio("Calle 2");
        paciente.setObraSocial(false);
        paciente.setHistorialClinico("Sin antecedentes");
        paciente = pacientes.save(paciente);
    }

    // ---------- Seguridad ----------

    @Test
    void sinSesionRedirigeAlLogin() throws Exception {
        mvc.perform(get("/turnos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void loginConCredencialesCorrectas() throws Exception {
        mvc.perform(formLogin("/login").user("jperez").password("clave123"))
                .andExpect(authenticated().withUsername("jperez").withRoles("MEDICO"));
    }

    @Test
    void loginConContraseñaIncorrecta() throws Exception {
        mvc.perform(formLogin("/login").user("jperez").password("otra"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "/", "/turnos", "/turnos/nuevo", "/calendario", "/medicos", "/medicos/nuevo",
            "/pacientes", "/pacientes/nuevo", "/usuarios", "/correo-prueba" })
    void adminAccedeASusPantallas(String url) throws Exception {
        mvc.perform(get(url).with(ADMIN)).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/turnos", "/calendario", "/medicos", "/usuarios", "/correo-prueba", "/pacientes/nuevo" })
    void medicoNoAccedeAPantallasDeAdmin(String url) throws Exception {
        mvc.perform(get(url).with(MEDICO)).andExpect(status().isForbidden());
    }

    @Test
    void medicoVeSuInicioYSuAgenda() throws Exception {
        mvc.perform(get("/").with(MEDICO))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hola, Juan Pérez")));
        mvc.perform(get("/mis-turnos").with(MEDICO)).andExpect(status().isOk());
    }

    // ---------- Turnos ----------

    @Test
    void crearTurnoValido() throws Exception {
        mvc.perform(nuevoTurno(paciente, enDias(3), "09:00"))
                .andExpect(redirectedUrl("/turnos?correo=pendiente")); // sin SMTP en tests

        assertThat(turnos.findAll()).hasSize(1);
    }

    @Test
    void noSePuedeAgendarParaHoyNiElPasado() throws Exception {
        mvc.perform(nuevoTurno(paciente, LocalDate.now(), "09:00"))
                .andExpect(model().attribute("error", containsString("al menos un día")));

        assertThat(turnos.findAll()).isEmpty();
    }

    @Test
    void noSePuedeAgendarUnaFechaInexistente() throws Exception {
        mvc.perform(post("/turnos/guardar").with(ADMIN)
                .param("medico", medico.getId().toString())
                .param("paciente", paciente.getId().toString())
                .param("dia", "31").param("mes", "2").param("anio", String.valueOf(enDias(400).getYear()))
                .param("hora", "09:00"))
                .andExpect(model().attribute("error", containsString("no es válida")));
    }

    @Test
    void elMedicoNoPuedeTenerDosTurnosALaMismaHora() throws Exception {
        LocalDate fecha = enDias(3);
        mvc.perform(nuevoTurno(paciente, fecha, "09:00"));

        Paciente otro = new Paciente();
        otro.setNombre("Pedro");
        otro.setApellido("López");
        otro.setDni("40999888");
        otro = pacientes.save(otro);

        mvc.perform(nuevoTurno(otro, fecha, "09:00"))
                .andExpect(model().attribute("error", containsString("El médico ya tiene un turno")));
        assertThat(turnos.findAll()).hasSize(1);
    }

    @Test
    void cancelarUnTurnoLiberaElHorario() throws Exception {
        LocalDate fecha = enDias(3);
        mvc.perform(nuevoTurno(paciente, fecha, "10:00"));
        Long id = turnos.findAll().getFirst().getId();

        mvc.perform(get("/turnos/eliminar/" + id).with(ADMIN))
                .andExpect(redirectedUrl("/turnos?cancelado=true"));
        assertThat(turnos.findById(id)).get().extracting(Turnos::isCancelado).isEqualTo(true);

        mvc.perform(nuevoTurno(paciente, fecha, "10:00"))
                .andExpect(redirectedUrl("/turnos?correo=pendiente"));
    }

    @Test
    void turnosOcupadosNoExponeDatosSensibles() throws Exception {
        mvc.perform(nuevoTurno(paciente, enDias(3), "09:00"));

        mvc.perform(get("/turnos/ocupados").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"medicoId\"")))
                .andExpect(content().string(not(containsString("clave123"))))
                .andExpect(content().string(not(containsString("antecedentes"))));
    }

    @Test
    void horariosDelMedicoSegunSuTurno() throws Exception {
        mvc.perform(get("/turnos/horarios/" + medico.getId()).with(ADMIN))
                .andExpect(content().string(containsString("\"08:00\"")))
                .andExpect(content().string(not(containsString("\"14:00\""))));
    }

    // ---------- Médicos ----------

    @Test
    void registrarMedicoCreaSuUsuarioDeAcceso() throws Exception {
        mvc.perform(post("/medicos/guardar").with(ADMIN)
                .param("nombre", "Ana").param("apellido", "Suárez").param("dni", "30999888")
                .param("telefono", "1").param("domicilio", "x").param("especialidad", "Pediatría")
                .param("horario", "Tarde (14:00 - 20:00)").param("usuario", "asuarez").param("contraseña", "secreto1"))
                .andExpect(redirectedUrl("/medicos?exito=medico_guardado"));

        assertThat(usuarios.findByUsername("asuarez")).get()
                .extracting(UserModel::getRol).isEqualTo("MEDICO");
    }

    @Test
    void noSePuedeRepetirElNombreDeUsuario() throws Exception {
        usuarios.save(usuario("admin", "admin", "ADMIN"));

        mvc.perform(post("/medicos/guardar").with(ADMIN)
                .param("nombre", "Ana").param("apellido", "Suárez").param("dni", "30999888")
                .param("usuario", "admin").param("contraseña", "secreto1"))
                .andExpect(model().attribute("error", containsString("ya está en uso")));
    }

    @Test
    void editarMedicoActualizaSuUsuarioDeAcceso() throws Exception {
        mvc.perform(post("/medicos/editar/" + medico.getId()).with(ADMIN)
                .param("nombre", "Juan").param("apellido", "Pérez").param("dni", "30111222")
                .param("especialidad", "Cardiología").param("horario", "Mañana (8:00 - 14:00)")
                .param("usuario", "juan.perez").param("contraseña", "nueva123"))
                .andExpect(redirectedUrl("/medicos?exito=medico_actualizado"));

        assertThat(usuarios.findByUsername("jperez")).isEmpty();
        assertThat(usuarios.findByUsername("juan.perez")).get()
                .extracting(UserModel::getPassword).isEqualTo("nueva123");
    }

    @Test
    void noSePuedeEliminarUnMedicoConTurnos() throws Exception {
        mvc.perform(nuevoTurno(paciente, enDias(3), "09:00"));

        mvc.perform(get("/medicos/eliminar/" + medico.getId()).with(ADMIN))
                .andExpect(redirectedUrl("/medicos?error=eliminacion"));
        assertThat(medicos.findById(medico.getId())).isPresent();
        assertThat(usuarios.findByUsername("jperez")).isPresent();
    }

    // ---------- Pacientes ----------

    @Test
    void elMedicoSoloModificaElHistorialClinico() throws Exception {
        mvc.perform(post("/pacientes/editar/" + paciente.getId()).with(MEDICO)
                .param("nombre", "Otro nombre")
                .param("historialClinico", "Control anual sin novedades"))
                .andExpect(redirectedUrl("/pacientes?exito=paciente_actualizado"));

        Paciente guardado = pacientes.findById(paciente.getId()).orElseThrow();
        assertThat(guardado.getNombre()).isEqualTo("María");
        assertThat(guardado.getHistorialClinico()).isEqualTo("Control anual sin novedades");
    }

    @Test
    void dniDuplicadoAlEditarConservaElPacienteQueSeEdita() throws Exception {
        Paciente otro = new Paciente();
        otro.setNombre("Pedro");
        otro.setApellido("López");
        otro.setDni("40999888");
        otro = pacientes.save(otro);

        mvc.perform(post("/pacientes/editar/" + otro.getId()).with(ADMIN)
                .param("nombre", "Pedro").param("apellido", "López").param("dni", paciente.getDni()))
                .andExpect(model().attribute("error", containsString("Ya existe otro paciente")))
                .andExpect(content().string(containsString("/pacientes/editar/" + otro.getId())));
    }

    // ---------- Auxiliares ----------

    private MockHttpServletRequestBuilder nuevoTurno(Paciente p, LocalDate fecha, String hora) {
        return post("/turnos/guardar").with(ADMIN)
                .param("medico", medico.getId().toString())
                .param("paciente", p.getId().toString())
                .param("dia", String.valueOf(fecha.getDayOfMonth()))
                .param("mes", String.valueOf(fecha.getMonthValue()))
                .param("anio", String.valueOf(fecha.getYear()))
                .param("hora", hora);
    }

    private static LocalDate enDias(int dias) {
        return LocalDate.now().plusDays(dias);
    }

    private static UserModel usuario(String username, String password, String rol) {
        UserModel u = new UserModel();
        u.setUsername(username);
        u.setPassword(password);
        u.setRol(rol);
        return u;
    }
}

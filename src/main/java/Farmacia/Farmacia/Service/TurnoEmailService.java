package Farmacia.Farmacia.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import Farmacia.Farmacia.Model.Paciente;
import Farmacia.Farmacia.Model.Turnos;
import java.util.Optional;

@Service
public class TurnoEmailService {

    private static final Logger logger = LoggerFactory.getLogger(TurnoEmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${MAIL_FROM:${MAIL_USERNAME:}}")
    private String mailFrom;

    public TurnoEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public Optional<String> enviarConfirmacion(Turnos turno) {
        Paciente paciente = turno.getPaciente();
        if (paciente == null) {
            return Optional.of("No se encontró al paciente del turno.");
        }
        if (paciente.getEmail() == null || paciente.getEmail().isBlank()) {
            return Optional.of("El paciente seleccionado no tiene un correo cargado en su ficha.");
        }
        if (mailHost == null || mailHost.isBlank()) {
            return Optional.of("No hay un servidor SMTP configurado en esta ejecución.");
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        if (!mailFrom.isBlank()) {
            mensaje.setFrom(mailFrom);
        }
        mensaje.setTo(paciente.getEmail());
        mensaje.setSubject("Confirmación de turno médico");
        mensaje.setText("Hola " + paciente.getNombre() + " " + paciente.getApellido() + ",\n\n"
                + "Tu turno fue agendado correctamente.\n"
                + "Fecha: " + turno.getDia() + "/" + turno.getMes() + "/" + turno.getAnio() + "\n"
                + "Hora: " + turno.getHora() + "\n"
                + "Profesional: " + turno.getMedico().getNombre() + " " + turno.getMedico().getApellido()
                + "\n\nSaludSync");

        try {
            mailSender.send(mensaje);
            return Optional.empty();
        } catch (MailException exception) {
            logger.warn("No se pudo enviar la confirmación del turno {}", turno.getId(), exception);
            Throwable causa = exception.getMostSpecificCause();
            String detalle = causa.getMessage();
            if (detalle == null || detalle.isBlank()) {
                detalle = exception.getMessage();
            }
            return Optional.of("El servidor SMTP rechazó el envío: " + detalle);
        }
    }

    public boolean enviarCorreoPrueba(String destinatario) {
        if (destinatario == null || destinatario.isBlank() || mailHost.isBlank()) {
            return false;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        if (!mailFrom.isBlank()) {
            mensaje.setFrom(mailFrom);
        }
        mensaje.setTo(destinatario.trim());
        mensaje.setSubject("Prueba de correo - SaludSync");
        mensaje.setText("Este es un correo de prueba enviado desde SaludSync.");

        try {
            mailSender.send(mensaje);
            return true;
        } catch (MailException exception) {
            logger.warn("No se pudo enviar el correo de prueba", exception);
            return false;
        }
    }
}
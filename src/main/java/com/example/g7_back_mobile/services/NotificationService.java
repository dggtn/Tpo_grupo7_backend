package com.example.g7_back_mobile.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.g7_back_mobile.repositories.ReservationRepository;
import com.example.g7_back_mobile.repositories.ShiftRepository;
import com.example.g7_back_mobile.repositories.UserRepository;
import com.example.g7_back_mobile.repositories.entities.Reservation;
import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.repositories.entities.User;

@Service
public class NotificationService {

    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ShiftRepository shiftRepository;
    
    @Autowired
    private EmailService emailService;

    /**
     * Tarea programada que se ejecuta cada 30 minutos para enviar notificaciones
     * de reservas que expiran pronto
     */
    @Scheduled(fixedRate = 1800000) // Cada 30 minutos
    public void notificarReservasProximasAExpirar() {
        try {
            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime enDosHoras = ahora.plusHours(2);
            
            // Buscar reservas que expiran en las próximas 2 horas
            List<Reservation> reservasProximasAExpirar = reservationRepository
                .findByExpiryDateBetween(ahora, enDosHoras);
            
            for (Reservation reserva : reservasProximasAExpirar) {
                try {
                    enviarNotificacionReservaProximaAExpirar(reserva);
                } catch (Exception e) {
                    System.err.println("[NotificationService] Error enviando notificación para reserva " 
                        + reserva.getId() + ": " + e.getMessage());
                }
            }
            
            if (!reservasProximasAExpirar.isEmpty()) {
                System.out.println("[NotificationService] Se enviaron " + reservasProximasAExpirar.size() 
                    + " notificaciones de reservas próximas a expirar");
            }
            
        } catch (Exception e) {
            System.err.println("[NotificationService.notificarReservasProximasAExpirar] Error: " + e.getMessage());
        }
    }

    private void enviarNotificacionReservaProximaAExpirar(Reservation reserva) {
        try {
            User user = userRepository.findById(reserva.getIdUser())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            Shift shift = shiftRepository.findById(reserva.getIdShift())
                .orElseThrow(() -> new RuntimeException("Cronograma no encontrado"));
            
            String nombreCurso = shift.getClase().getName();
            String diaClase = obtenerNombreDia(shift.getDiaEnQueSeDicta());
            String horaClase = shift.getHoraInicio();
            
            // Calcular tiempo restante
            LocalDateTime ahora = LocalDateTime.now();
            long minutosRestantes = java.time.Duration.between(ahora, reserva.getExpiryDate()).toMinutes();
            
            String subject = "⏰ Tu reserva para " + nombreCurso + " expira pronto";
            
            String body = String.format(
                "Hola %s,\n\n" +
                "Te recordamos que tu reserva para el curso '%s' expira en aproximadamente %d minutos.\n\n" +
                "Detalles de tu reserva:\n" +
                "- Curso: %s\n" +
                "- Día: %s a las %s hs\n" +
                "- Fecha límite de reserva: %s\n\n" +
                "Para conservar tu lugar, puedes:\n" +
                "1. Inscribirte formalmente al curso antes de que expire la reserva\n" +
                "2. Asistir a la primera clase y tu inscripción se realizará automáticamente\n\n" +
                "Si no realizas ninguna acción, tu reserva se cancelará automáticamente.\n\n" +
                "¡No te pierdas esta oportunidad!\n\n" +
                "Saludos,\n" +
                "El equipo de RitmoFit",
                user.getUsername(),
                nombreCurso,
                minutosRestantes,
                nombreCurso,
                diaClase,
                horaClase,
                reserva.getExpiryDate()
            );
            
            emailService.sendEmail(user.getEmail(), subject, body);
            System.out.println("[NotificationService] Notificación enviada a " + user.getEmail() 
                + " para reserva que expira en " + minutosRestantes + " minutos");
            
        } catch (Exception e) {
            System.err.println("[NotificationService.enviarNotificacionReservaProximaAExpirar] Error: " + e.getMessage());
            throw e;
        }
    }
    
    private String obtenerNombreDia(int diaNumero) {
        String[] diasSemana = {"", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        return (diaNumero >= 1 && diaNumero <= 7) ? diasSemana[diaNumero] : "Día " + diaNumero;
    }
}

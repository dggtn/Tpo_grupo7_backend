package com.example.g7_back_mobile.services;

import com.example.g7_back_mobile.repositories.UserEventRepository;
import com.example.g7_back_mobile.repositories.entities.UserEvent;
import com.example.g7_back_mobile.repositories.entities.Course;
import com.example.g7_back_mobile.repositories.entities.Shift;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventService {
    
    private final UserEventRepository eventRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Crear evento de inscripción
     */
    @Async
    @Transactional
    public void createEnrollmentEvent(Long userId, Course course, Shift shift) {
        try {
            LocalDateTime classStart = calculateNextClassDateTime(shift);
            
            // ✅ Parsear horaInicio como LocalTime antes de formatear
            LocalTime horaInicioTime = LocalTime.parse(shift.getHoraInicio());
            
            String message = String.format(
                "Te has inscrito en %s. Próxima clase: %s a las %s en %s",
                course.getName(),
                classStart.format(DATE_FORMATTER),
                horaInicioTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                shift.getSede() != null ? shift.getSede().getName() : "sede por confirmar"
            );
            
            UserEvent event = UserEvent.builder()
                .userId(userId)
                .type(UserEvent.EventType.ENROLLED)
                .title("Inscripción Confirmada")
                .message(message)
                .classId(course.getId())
                .shiftId(shift.getId())
                .classStartAt(classStart)
                .courseName(course.getName())
                .sede(shift.getSede() != null ? shift.getSede().getName() : null)
                .build();
            
            eventRepository.save(event);
            log.info("[UserEventService] Evento ENROLLED creado para userId={}, shiftId={}", 
                userId, shift.getId());
            
        } catch (Exception e) {
            log.error("[UserEventService] Error creando evento de inscripción: {}", e.getMessage());
        }
    }
    
    /**
     * Crear evento de reprogramación
     */
    @Async
    @Transactional
    public void createRescheduleEvent(Long userId, Course course, Shift shift, 
                                     LocalDateTime newStartTime, String reason) {
        try {
            String message = String.format(
                "La clase de %s ha sido reprogramada para el %s a las %s. %s",
                course.getName(),
                newStartTime.format(DATE_FORMATTER),
                newStartTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                reason != null ? "Motivo: " + reason : ""
            );
            
            UserEvent event = UserEvent.builder()
                .userId(userId)
                .type(UserEvent.EventType.RESCHEDULE)
                .title("Clase Reprogramada")
                .message(message)
                .classId(course.getId())
                .shiftId(shift.getId())
                .classStartAt(newStartTime)
                .courseName(course.getName())
                .sede(shift.getSede() != null ? shift.getSede().getName() : null)
                .build();
            
            eventRepository.save(event);
            log.info("[UserEventService] Evento RESCHEDULE creado para userId={}, shiftId={}", 
                userId, shift.getId());
            
        } catch (Exception e) {
            log.error("[UserEventService] Error creando evento de reprogramación: {}", e.getMessage());
        }
    }
    
    /**
     * Crear evento de cancelación
     */
    @Async
    @Transactional
    public void createCancellationEvent(Long userId, Course course, Shift shift, String reason) {
        try {
            String message = String.format(
                "La clase de %s del %s ha sido cancelada. %s",
                course.getName(),
                calculateNextClassDateTime(shift).format(DATE_FORMATTER),
                reason != null ? "Motivo: " + reason : ""
            );
            
            UserEvent event = UserEvent.builder()
                .userId(userId)
                .type(UserEvent.EventType.CANCEL)
                .title("Clase Cancelada")
                .message(message)
                .classId(course.getId())
                .shiftId(shift.getId())
                .courseName(course.getName())
                .build();
            
            eventRepository.save(event);
            log.info("[UserEventService] Evento CANCEL creado para userId={}, shiftId={}", 
                userId, shift.getId());
            
        } catch (Exception e) {
            log.error("[UserEventService] Error creando evento de cancelación: {}", e.getMessage());
        }
    }
    
    /**
     * Crear evento de confirmación de reserva
     */
    @Async
    @Transactional
    public void createReservationConfirmedEvent(Long userId, Course course, Shift shift, 
                                               LocalDateTime expiryDate) {
        try {
            String message = String.format(
                "Reserva confirmada para %s. Recuerda asistir a la primera clase el %s para confirmar tu inscripción.",
                course.getName(),
                calculateNextClassDateTime(shift).format(DATE_FORMATTER)
            );
            
            UserEvent event = UserEvent.builder()
                .userId(userId)
                .type(UserEvent.EventType.RESERVATION_CONFIRMED)
                .title("Reserva Confirmada")
                .message(message)
                .classId(course.getId())
                .shiftId(shift.getId())
                .classStartAt(calculateNextClassDateTime(shift))
                .courseName(course.getName())
                .sede(shift.getSede() != null ? shift.getSede().getName() : null)
                .build();
            
            eventRepository.save(event);
            log.info("[UserEventService] Evento RESERVATION_CONFIRMED creado para userId={}, shiftId={}", 
                userId, shift.getId());
            
        } catch (Exception e) {
            log.error("[UserEventService] Error creando evento de reserva: {}", e.getMessage());
        }
    }
    
    /**
     * Crear evento de reserva expirada
     */
    @Async
    @Transactional
    public void createReservationExpiredEvent(Long userId, Course course, Shift shift) {
        try {
            String message = String.format(
                "Tu reserva para %s ha expirado. El cupo ha sido liberado.",
                course.getName()
            );
            
            UserEvent event = UserEvent.builder()
                .userId(userId)
                .type(UserEvent.EventType.RESERVATION_EXPIRED)
                .title("Reserva Expirada")
                .message(message)
                .classId(course.getId())
                .shiftId(shift.getId())
                .courseName(course.getName())
                .build();
            
            eventRepository.save(event);
            log.info("[UserEventService] Evento RESERVATION_EXPIRED creado para userId={}, shiftId={}", 
                userId, shift.getId());
            
        } catch (Exception e) {
            log.error("[UserEventService] Error creando evento de expiración: {}", e.getMessage());
        }
    }
    
    /**
     * Obtener eventos desde un timestamp (para long polling)
     */
    @Transactional
    public List<UserEvent> getEventsSince(Long userId, LocalDateTime since) {
        return eventRepository.findUnconsumedEventsSince(userId, since);
    }
    
    /**
     * Marcar eventos como consumidos
     */
    @Transactional
    public void markEventsAsConsumed(List<Long> eventIds) {
        if (eventIds != null && !eventIds.isEmpty()) {
            eventRepository.markAsConsumed(eventIds, LocalDateTime.now());
            log.debug("[UserEventService] Marcados {} eventos como consumidos", eventIds.size());
        }
    }
    
    /**
     * Limpieza periódica de eventos antiguos (ejecutar diariamente)
     */
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM todos los días
    @Transactional
    public void cleanupOldEvents() {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(7);
            int deleted = eventRepository.deleteConsumedEventsBefore(threshold);
            log.info("[UserEventService] Limpieza: {} eventos antiguos eliminados", deleted);
        } catch (Exception e) {
            log.error("[UserEventService] Error en limpieza de eventos: {}", e.getMessage());
        }
    }
    
    /**
     * Calcular la próxima fecha/hora de clase
     */
    private LocalDateTime calculateNextClassDateTime(Shift shift) {
        LocalDateTime now = LocalDateTime.now();
        
        // ✅ Parsear horaInicio como LocalTime
        LocalTime horaInicioTime = LocalTime.parse(shift.getHoraInicio());
        
        LocalDateTime courseStart = shift.getClase().getFechaInicio().atTime(horaInicioTime);
        
        if (courseStart.isAfter(now)) {
            return courseStart;
        }
        
        // Si el curso ya empezó, calcular la próxima clase
        int dayOfWeek = shift.getDiaEnQueSeDicta();
        LocalDateTime nextClass = now;
        
        while (nextClass.getDayOfWeek().getValue() != dayOfWeek || 
               nextClass.isBefore(now)) {
            nextClass = nextClass.plusDays(1);
        }
        
        // ✅ Usar horaInicioTime ya parseado
        return nextClass.withHour(horaInicioTime.getHour())
                        .withMinute(horaInicioTime.getMinute())
                        .withSecond(0)
                        .withNano(0);
    }
}

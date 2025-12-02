package com.example.g7_back_mobile.services;

import com.example.g7_back_mobile.controllers.dtos.UserEventDTO;
import com.example.g7_back_mobile.repositories.InscriptionRepository;
import com.example.g7_back_mobile.repositories.ReservationRepository;
import com.example.g7_back_mobile.repositories.UserEventRepository;
import com.example.g7_back_mobile.repositories.entities.Course;
import com.example.g7_back_mobile.repositories.entities.Inscription;
import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.repositories.entities.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserEventService {

    @Autowired
    private UserEventRepository userEventRepository;
    
    @Autowired
    private InscriptionRepository inscriptionRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Obtiene eventos pendientes para un usuario (long polling)
     * Solo devuelve eventos cuya hora programada ya llegó
     */
    public List<UserEventDTO> getPendingEvents(Long userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<UserEvent> events = userEventRepository.findPendingEventsByUserId(userId, now);
            
            if (!events.isEmpty()) {
                // Marcar como entregados
                List<Long> eventIds = events.stream()
                    .map(UserEvent::getId)
                    .collect(Collectors.toList());
                markAsDelivered(eventIds);
                
                System.out.println("[UserEventService.getPendingEvents] Entregando " + events.size() 
                    + " eventos para usuario " + userId);
            }
            
            return events.stream()
                .map(UserEventDTO::fromEntity)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("[UserEventService.getPendingEvents] Error: " + e.getMessage());
            e.printStackTrace();
            // Devolver lista vacía en caso de error para no romper el polling
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Marca eventos como entregados
     */
    @Transactional
    public void markAsDelivered(List<Long> eventIds) {
        try {
            if (eventIds == null || eventIds.isEmpty()) return;
            
            userEventRepository.markAsDelivered(eventIds, LocalDateTime.now());
            userEventRepository.flush();
            
            System.out.println("[UserEventService.markAsDelivered] " + eventIds.size() + " eventos marcados");
            
        } catch (Exception e) {
            System.err.println("[UserEventService.markAsDelivered] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Marca eventos como leídos
     */
    @Transactional
    public void markAsRead(List<Long> eventIds) {
        try {
            if (eventIds == null || eventIds.isEmpty()) return;
            
            userEventRepository.markAsRead(eventIds);
            userEventRepository.flush();
            
            System.out.println("[UserEventService.markAsRead] " + eventIds.size() + " eventos marcados como leídos");
            
        } catch (Exception e) {
            System.err.println("[UserEventService.markAsRead] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cuenta eventos no leídos
     */
    public long countUnreadEvents(Long userId) {
        try {
            return userEventRepository.countByUserIdAndReadFalse(userId);
        } catch (Exception e) {
            System.err.println("[UserEventService.countUnreadEvents] Error: " + e.getMessage());
            return 0;
        }
    }

    /**
     * ✅ CORREGIDO: Crea un evento de recordatorio de clase (1h antes)
     * Ahora incluye classTime en el metadata
     */
    public void createClassReminderEvent(Long userId, Course course, Shift shift, LocalDateTime classTime) {
        try {
            // Verificar que la clase sea futura
            if (classTime.isBefore(LocalDateTime.now())) {
                System.out.println("[UserEventService] ⏭️ Clase en el pasado, omitiendo: " + classTime);
                return;
            }
            
            LocalDateTime reminderTime = classTime.minusHours(1);
            
            // Evitar duplicados
            List<UserEvent> existing = userEventRepository.findByUserIdAndRelatedShiftIdAndEventType(
                userId, shift.getId(), UserEvent.EventType.CLASS_REMINDER);
            
            boolean alreadyExists = existing.stream()
                .anyMatch(e -> {
                    try {
                        long diffMinutes = Math.abs(
                            java.time.Duration.between(e.getScheduledTime(), reminderTime).toMinutes()
                        );
                        return diffMinutes < 1;
                    } catch (Exception ex) {
                        return false;
                    }
                });
            
            if (alreadyExists) {
                System.out.println("[UserEventService] ⏭️ Recordatorio ya existe para userId=" + userId 
                    + ", shiftId=" + shift.getId() + ", time=" + classTime);
                return;
            }

            // ✅ IMPORTANTE: Incluir classTime en metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("classTime", classTime.toString()); // ✅ CAMPO CRÍTICO
            metadata.put("courseName", course.getName());
            metadata.put("shiftHour", shift.getHoraInicio());
            metadata.put("dayOfWeek", shift.getDiaEnQueSeDicta());
            if (shift.getSede() != null) {
                metadata.put("location", shift.getSede().getName());
            }

            UserEvent event = UserEvent.builder()
                .userId(userId)
                .eventType(UserEvent.EventType.CLASS_REMINDER)
                .title("¡Clase en 1 hora!")
                .message(String.format("Tu clase de %s comienza a las %s. ¡No la pierdas!", 
                    course.getName(), shift.getHoraInicio()))
                .relatedShiftId(shift.getId())
                .relatedCourseId(course.getId())
                .scheduledTime(reminderTime) // 1 hora antes
                .metadata(objectMapper.writeValueAsString(metadata))
                .build();

            userEventRepository.save(event);
            System.out.println("[UserEventService] ✅ Recordatorio creado para userId=" + userId 
                + " el " + reminderTime + " (clase a las " + classTime + ")");
            
        } catch (Exception e) {
            System.err.println("[UserEventService.createClassReminderEvent] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea evento de clase cancelada
     */
    public void createClassCancelledEvent(Long userId, Course course, Shift shift, String reason) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("courseName", course.getName());
            metadata.put("shiftHour", shift.getHoraInicio());
            metadata.put("reason", reason);

            UserEvent event = UserEvent.builder()
                .userId(userId)
                .eventType(UserEvent.EventType.CLASS_CANCELLED)
                .title("Clase cancelada")
                .message(String.format("La clase de %s del %s ha sido cancelada. %s", 
                    course.getName(), obtenerNombreDia(shift.getDiaEnQueSeDicta()), reason))
                .relatedShiftId(shift.getId())
                .relatedCourseId(course.getId())
                .scheduledTime(LocalDateTime.now()) // Inmediato
                .metadata(objectMapper.writeValueAsString(metadata))
                .build();

            userEventRepository.save(event);
            System.out.println("[UserEventService] Evento de cancelación creado para userId=" + userId);
            
        } catch (Exception e) {
            System.err.println("[UserEventService.createClassCancelledEvent] Error: " + e.getMessage());
        }
    }

    /**
     * ✅ CORREGIDO: Crea evento de clase reprogramada con classTime
     */
    public void createClassRescheduledEvent(Long userId, Course course, Shift oldShift, 
                                           Shift newShift, LocalDateTime newClassTime) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("courseName", course.getName());
            metadata.put("oldTime", oldShift.getHoraInicio());
            metadata.put("newTime", newShift.getHoraInicio());
            metadata.put("classTime", newClassTime.toString()); // ✅ CAMPO CRÍTICO

            UserEvent event = UserEvent.builder()
                .userId(userId)
                .eventType(UserEvent.EventType.CLASS_RESCHEDULED)
                .title("Clase reprogramada")
                .message(String.format("La clase de %s se ha reprogramado. Nuevo horario: %s a las %s", 
                    course.getName(), newClassTime.toLocalDate(), newShift.getHoraInicio()))
                .relatedShiftId(newShift.getId())
                .relatedCourseId(course.getId())
                .scheduledTime(LocalDateTime.now())
                .metadata(objectMapper.writeValueAsString(metadata))
                .build();

            userEventRepository.save(event);
            
        } catch (Exception e) {
            System.err.println("[UserEventService.createClassRescheduledEvent] Error: " + e.getMessage());
        }
    }

    /**
     * ✅ CORREGIDO: Crea evento de reserva confirmada con classTime
     */
    public void createReservationConfirmedEvent(Long userId, Course course, Shift shift, 
                                               LocalDateTime expiryDate) {
        try {
            // ✅ CALCULAR LA FECHA/HORA DE LA PRIMERA CLASE
            LocalDateTime firstClassTime = calculateNextClassTime(shift, LocalDateTime.now());
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("courseName", course.getName());
            metadata.put("expiryDate", expiryDate.toString());
            
            // ✅ AGREGAR classTime para que el frontend pueda programar recordatorios
            if (firstClassTime != null) {
                metadata.put("classTime", firstClassTime.toString());
                System.out.println("[UserEventService] ✅ Primera clase programada para: " + firstClassTime);
            }

            UserEvent event = UserEvent.builder()
                .userId(userId)
                .eventType(UserEvent.EventType.RESERVATION_CONFIRMED)
                .title("Reserva confirmada")
                .message(String.format("Tu reserva para %s ha sido confirmada. " +
                    "Vence el %s", course.getName(), expiryDate.toLocalDate()))
                .relatedShiftId(shift.getId())
                .relatedCourseId(course.getId())
                .scheduledTime(LocalDateTime.now())
                .metadata(objectMapper.writeValueAsString(metadata))
                .build();

            userEventRepository.save(event);
            System.out.println("[UserEventService] ✅ Evento de reserva confirmada creado con classTime");
            
        } catch (Exception e) {
            System.err.println("[UserEventService.createReservationConfirmedEvent] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea evento de reserva por expirar
     */
    public void createReservationExpiringEvent(Long userId, Course course, Shift shift, 
                                              long minutesRemaining) {
        try {
            UserEvent event = UserEvent.builder()
                .userId(userId)
                .eventType(UserEvent.EventType.RESERVATION_EXPIRING)
                .title("Reserva por expirar")
                .message(String.format("Tu reserva para %s expira en %d minutos. " +
                    "Inscríbete pronto para no perder tu lugar.", course.getName(), minutesRemaining))
                .relatedShiftId(shift.getId())
                .relatedCourseId(course.getId())
                .scheduledTime(LocalDateTime.now())
                .build();

            userEventRepository.save(event);
            
        } catch (Exception e) {
            System.err.println("[UserEventService.createReservationExpiringEvent] Error: " + e.getMessage());
        }
    }

    /**
     * Crea evento de reserva expirada
     */
    public void createReservationExpiredEvent(Long userId, Course course, Shift shift) {
        try {
            UserEvent event = UserEvent.builder()
                .userId(userId)
                .eventType(UserEvent.EventType.RESERVATION_EXPIRED)
                .title("Reserva expirada")
                .message(String.format("Tu reserva para %s ha expirado. " +
                    "Puedes hacer una nueva reserva si aún hay cupos disponibles.", course.getName()))
                .relatedShiftId(shift.getId())
                .relatedCourseId(course.getId())
                .scheduledTime(LocalDateTime.now())
                .build();

            userEventRepository.save(event);
            
        } catch (Exception e) {
            System.err.println("[UserEventService.createReservationExpiredEvent] Error: " + e.getMessage());
        }
    }

    /**
     * ✅ CORREGIDO: Crea evento de inscripción confirmada con classTime
     */
    public void createEnrollmentEvent(Long userId, Course course, Shift shift) {
        try {
            // ✅ CALCULAR LA PRÓXIMA CLASE
            LocalDateTime nextClassTime = calculateNextClassTime(shift, LocalDateTime.now());
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("courseName", course.getName());
            metadata.put("shiftHour", shift.getHoraInicio());
            metadata.put("dayOfWeek", shift.getDiaEnQueSeDicta());
            
            // ✅ AGREGAR classTime
            if (nextClassTime != null) {
                metadata.put("classTime", nextClassTime.toString());
                System.out.println("[UserEventService] ✅ Próxima clase: " + nextClassTime);
            }
            
            UserEvent event = UserEvent.builder()
                .userId(userId)
                .eventType(UserEvent.EventType.ENROLLMENT_CONFIRMED)
                .title("¡Inscripción exitosa!")
                .message(String.format("Te has inscrito exitosamente a %s. " +
                    "Tu clase es los %s a las %s.", 
                    course.getName(), 
                    obtenerNombreDia(shift.getDiaEnQueSeDicta()),
                    shift.getHoraInicio()))
                .relatedShiftId(shift.getId())
                .relatedCourseId(course.getId())
                .scheduledTime(LocalDateTime.now())
                .metadata(objectMapper.writeValueAsString(metadata))
                .build();

            userEventRepository.save(event);
            System.out.println("[UserEventService] ✅ Evento de inscripción creado con classTime");
            
        } catch (Exception e) {
            System.err.println("[UserEventService.createEnrollmentEvent] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea evento de inscripción/reserva cancelada
     */
    public void createCancellationEvent(Long userId, Course course, Shift shift, String reason) {
        try {
            UserEvent event = UserEvent.builder()
                .userId(userId)
                .eventType(UserEvent.EventType.ENROLLMENT_CANCELLED)
                .title("Cancelación confirmada")
                .message(String.format("Tu inscripción a %s ha sido cancelada. %s", 
                    course.getName(), reason))
                .relatedShiftId(shift.getId())
                .relatedCourseId(course.getId())
                .scheduledTime(LocalDateTime.now())
                .build();

            userEventRepository.save(event);
            
        } catch (Exception e) {
            System.err.println("[UserEventService.createCancellationEvent] Error: " + e.getMessage());
        }
    }

    /**
     * Tarea programada: genera recordatorios para clases próximas
     * Se ejecuta cada 15 minutos
     */
    @Scheduled(fixedRate = 900000) // 15 minutos
    @Transactional
    public void generateUpcomingClassReminders() {
        try {
            System.out.println("[UserEventService.generateUpcomingClassReminders] Iniciando generación...");
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime windowStart = now.plusMinutes(45); // Desde 45 min adelante
            LocalDateTime windowEnd = now.plusMinutes(75);   // Hasta 1h15 adelante
            
            // Obtener todas las inscripciones activas
            List<Inscription> activeInscriptions = inscriptionRepository.findByEstado("ACTIVA");
            
            int remindersCreated = 0;
            
            for (Inscription inscription : activeInscriptions) {
                try {
                    Shift shift = inscription.getShift();
                    Course course = shift.getClase();
                    
                    // Calcular próxima clase
                    LocalDateTime nextClass = calculateNextClassTime(shift, now);
                    
                    if (nextClass == null) continue;
                    
                    // Verificar si está en la ventana de 45-75 minutos
                    if (nextClass.isAfter(windowStart) && nextClass.isBefore(windowEnd)) {
                        createClassReminderEvent(
                            inscription.getUser().getId(),
                            course,
                            shift,
                            nextClass
                        );
                        remindersCreated++;
                    }
                    
                } catch (Exception e) {
                    System.err.println("[UserEventService] Error procesando inscripción " 
                        + inscription.getId() + ": " + e.getMessage());
                }
            }
            
            if (remindersCreated > 0) {
                System.out.println("[UserEventService.generateUpcomingClassReminders] " 
                    + remindersCreated + " recordatorios creados");
            }
            
        } catch (Exception e) {
            System.err.println("[UserEventService.generateUpcomingClassReminders] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ MÉTODO PÚBLICO: Calcula la próxima clase basándose en el día y hora del shift
     */
    public LocalDateTime calculateNextClassTime(Shift shift, LocalDateTime from) {
        try {
            int targetDay = shift.getDiaEnQueSeDicta(); // 1=Lunes, 7=Domingo
            LocalTime classTime = LocalTime.parse(shift.getHoraInicio());
            
            LocalDate today = from.toLocalDate();
            DayOfWeek targetDayOfWeek = DayOfWeek.of(targetDay);
            
            // Buscar el próximo día que coincida
            LocalDate nextDate = today;
            int daysChecked = 0;
            
            while (daysChecked < 14) { // Buscar hasta 2 semanas adelante
                if (nextDate.getDayOfWeek() == targetDayOfWeek) {
                    LocalDateTime candidate = LocalDateTime.of(nextDate, classTime);
                    
                    // Solo si es futuro
                    if (candidate.isAfter(from)) {
                        // Verificar que esté dentro del rango del curso
                        if (!nextDate.isBefore(shift.getClase().getFechaInicio()) &&
                            !nextDate.isAfter(shift.getClase().getFechaFin())) {
                            return candidate;
                        }
                    }
                }
                nextDate = nextDate.plusDays(1);
                daysChecked++;
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("[UserEventService.calculateNextClassTime] Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Limpieza de eventos antiguos - ejecutar semanalmente
     */
    @Scheduled(cron = "0 0 2 * * SUN") // Domingos a las 2 AM
    @Transactional
    public void cleanupOldEvents() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            userEventRepository.deleteOldReadEvents(cutoffDate);
            System.out.println("[UserEventService.cleanupOldEvents] Eventos antiguos eliminados");
        } catch (Exception e) {
            System.err.println("[UserEventService.cleanupOldEvents] Error: " + e.getMessage());
        }
    }

    private String obtenerNombreDia(int diaNumero) {
        String[] diasSemana = {"", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        return (diaNumero >= 1 && diaNumero <= 7) ? diasSemana[diaNumero] : "Día " + diaNumero;
    }
}
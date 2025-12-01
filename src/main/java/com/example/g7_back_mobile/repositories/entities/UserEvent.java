package com.example.g7_back_mobile.repositories.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "user_events")
public class UserEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "event_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "message", nullable = false, length = 1000)
    private String message;
    
    @Column(name = "related_shift_id")
    private Long relatedShiftId;
    
    @Column(name = "related_course_id")
    private Long relatedCourseId;
    
    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "read", nullable = false)
    private Boolean read;
    
    @Column(name = "delivered", nullable = false)
    private Boolean delivered;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "metadata", length = 2000)
    private String metadata; // JSON con información adicional
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (read == null) read = false;
        if (delivered == null) delivered = false;
    }
    
    public enum EventType {
        CLASS_REMINDER,           // Recordatorio 1h antes de clase
        CLASS_CANCELLED,          // Clase cancelada
        CLASS_RESCHEDULED,        // Clase reprogramada
        RESERVATION_CONFIRMED,    // Reserva confirmada
        RESERVATION_EXPIRING,     // Reserva por expirar
        RESERVATION_EXPIRED,      // Reserva expirada
        ENROLLMENT_CONFIRMED,     // Inscripción confirmada
        ENROLLMENT_CANCELLED      // Inscripción cancelada
    }
}
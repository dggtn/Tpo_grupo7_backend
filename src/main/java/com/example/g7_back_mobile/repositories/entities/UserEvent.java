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
@Table(name = "user_events", indexes = {
    @Index(name = "idx_user_timestamp", columnList = "user_id,created_at"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class UserEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EventType type;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(length = 100)
    private String title;
    
    @Column(name = "class_id")
    private Long classId; // ID del curso
    
    @Column(name = "shift_id")
    private Long shiftId;
    
    @Column(name = "reservation_id")
    private Long reservationId;
    
    @Column(name = "inscription_id")
    private Long inscriptionId;
    
    @Column(name = "class_start_at")
    private LocalDateTime classStartAt; // Fecha/hora de inicio de la clase
    
    @Column(name = "sede")
    private String sede;
    
    @Column(name = "course_name")
    private String courseName;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "consumed", nullable = false)
    private Boolean consumed = false;
    
    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (consumed == null) {
            consumed = false;
        }
    }
    
    // Enum para tipos de eventos
    public enum EventType {
        ENROLLED,              // Usuario inscrito en curso
        NEW_CLASS,             // Nueva clase disponible
        CLASS_ASSIGNED,        // Clase asignada al usuario
        RESCHEDULE,            // Clase reprogramada
        CANCEL,                // Clase cancelada
        REMINDER,              // Recordatorio de clase
        RESERVATION_CONFIRMED, // Reserva confirmada
        RESERVATION_EXPIRED    // Reserva expirada
    }
}

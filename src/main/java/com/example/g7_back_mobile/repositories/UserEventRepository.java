package com.example.g7_back_mobile.repositories;

import com.example.g7_back_mobile.repositories.entities.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
    
    // Eventos pendientes (no leídos) de un usuario
    List<UserEvent> findByUserIdAndReadFalseOrderByScheduledTimeAsc(Long userId);
    
    // Eventos no entregados de un usuario
    List<UserEvent> findByUserIdAndDeliveredFalseOrderByScheduledTimeAsc(Long userId);
    
    // Eventos pendientes cuya hora programada ya llegó
    @Query("SELECT e FROM UserEvent e WHERE e.userId = :userId AND e.delivered = false " +
           "AND e.scheduledTime <= :now ORDER BY e.scheduledTime ASC")
    List<UserEvent> findPendingEventsByUserId(@Param("userId") Long userId, 
                                               @Param("now") LocalDateTime now);
    
    // Marcar eventos como entregados
    @Modifying
    @Query("UPDATE UserEvent e SET e.delivered = true, e.deliveredAt = :deliveredAt " +
           "WHERE e.id IN :eventIds")
    void markAsDelivered(@Param("eventIds") List<Long> eventIds, 
                         @Param("deliveredAt") LocalDateTime deliveredAt);
    
    // Marcar eventos como leídos
    @Modifying
    @Query("UPDATE UserEvent e SET e.read = true WHERE e.id IN :eventIds")
    void markAsRead(@Param("eventIds") List<Long> eventIds);
    
    // Eventos de un turno específico (para evitar duplicados)
    List<UserEvent> findByUserIdAndRelatedShiftIdAndEventType(
        Long userId, Long shiftId, UserEvent.EventType eventType);
    
    // Limpiar eventos antiguos ya leídos (para no llenar la DB)
    @Modifying
    @Query("DELETE FROM UserEvent e WHERE e.read = true AND e.createdAt < :cutoffDate")
    void deleteOldReadEvents(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Contar eventos no leídos
    long countByUserIdAndReadFalse(Long userId);
}
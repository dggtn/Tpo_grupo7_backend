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
    
    /**
     * Obtener eventos no consumidos de un usuario desde un timestamp
     */
    @Query("SELECT e FROM UserEvent e WHERE e.userId = :userId " +
           "AND e.consumed = false " +
           "AND e.createdAt > :since " +
           "ORDER BY e.createdAt ASC")
    List<UserEvent> findUnconsumedEventsSince(
        @Param("userId") Long userId, 
        @Param("since") LocalDateTime since
    );
    
    /**
     * Marcar eventos como consumidos
     */
    @Modifying
    @Query("UPDATE UserEvent e SET e.consumed = true, e.consumedAt = :consumedAt " +
           "WHERE e.id IN :eventIds")
    void markAsConsumed(
        @Param("eventIds") List<Long> eventIds, 
        @Param("consumedAt") LocalDateTime consumedAt
    );
    
    /**
     * Limpiar eventos antiguos consumidos (limpieza periódica)
     */
    @Modifying
    @Query("DELETE FROM UserEvent e WHERE e.consumed = true " +
           "AND e.consumedAt < :before")
    int deleteConsumedEventsBefore(@Param("before") LocalDateTime before);
    
    /**
     * Obtener eventos pendientes de un usuario para un shift específico
     */
    @Query("SELECT e FROM UserEvent e WHERE e.userId = :userId " +
           "AND e.shiftId = :shiftId " +
           "AND e.consumed = false " +
           "ORDER BY e.createdAt DESC")
    List<UserEvent> findPendingEventsByShift(
        @Param("userId") Long userId, 
        @Param("shiftId") Long shiftId
    );
    
    /**
     * Contar eventos no consumidos de un usuario
     */
    @Query("SELECT COUNT(e) FROM UserEvent e WHERE e.userId = :userId " +
           "AND e.consumed = false")
    long countUnconsumedEvents(@Param("userId") Long userId);
}


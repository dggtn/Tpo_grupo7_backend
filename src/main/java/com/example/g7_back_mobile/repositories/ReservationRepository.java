package com.example.g7_back_mobile.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.g7_back_mobile.repositories.entities.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
   
    List<Reservation> findByIdUser(Long userId);
    List<Reservation> findByIdShift(Long shiftId);
    Optional<Reservation> findByIdUserAndIdShift(Long userId, Long shiftId);
    
    // Nuevo método para encontrar reservas expiradas
    List<Reservation> findByExpiryDateBefore(LocalDateTime dateTime);
    
    // Método para encontrar reservas que expiran pronto (para notificaciones)
    List<Reservation> findByExpiryDateBetween(LocalDateTime start, LocalDateTime end);
	

}
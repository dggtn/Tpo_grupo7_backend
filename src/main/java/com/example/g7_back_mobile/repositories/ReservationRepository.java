package com.example.g7_back_mobile.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.g7_back_mobile.repositories.entities.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
   
    List<Reservation> findByIdUser(Long userId);
    List<Reservation> findByIdShift(Long shiftId);

}

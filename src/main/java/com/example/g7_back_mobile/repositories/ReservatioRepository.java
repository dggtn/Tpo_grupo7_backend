package com.example.g7_back_mobile.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.g7_back_mobile.repositories.entities.Reservation;
import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.repositories.entities.User;

@Repository
public interface ReservatioRepository extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByUser(User user);
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByShift(Shift courseSchedule);
    List<Reservation> findShiftId(Long courseScheduleId);
 
    Optional<Reservation> findByUserAndShift(User user, Shift shift);

}

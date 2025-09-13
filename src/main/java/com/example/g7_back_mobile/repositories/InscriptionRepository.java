package com.example.g7_back_mobile.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.g7_back_mobile.repositories.entities.Inscription;
import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.repositories.entities.User;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    
    List<Inscription> findByUser(User user);
    List<Inscription> findByUserId(Long userId);
    List<Inscription> findByShift(Shift shift);
    List<Inscription> findByShiftId(Long shiftId);
    List<Inscription> findByEstado(String estado);
    Optional<Inscription> findByUserAndShift(User user, Shift shift);
    @Query("SELECT COUNT(i) FROM Inscription i WHERE i.shift.id = :shiftId AND i.estado = 'ACTIVA'")
    Long countActiveByShift(@Param("shiftId") Long shiftId);
    Optional<Inscription> findByUserIdAndShiftIdAndEstado(Long userId, Long shiftId, String estado);
    List<Inscription> findByUserIdAndEstado(Long userId, String estado);

}

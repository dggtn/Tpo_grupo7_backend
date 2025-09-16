package com.example.g7_back_mobile.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.g7_back_mobile.repositories.entities.CourseAttend;
import com.example.g7_back_mobile.repositories.entities.Inscription;

@Repository
public interface CourseAttendRepository extends JpaRepository<CourseAttend, Long> {
    
    // Consultas existentes
    boolean existsByInscripcionAndFechaAsistencia(Inscription inscripcion, LocalDate fecha);
    long countByInscripcion(Inscription inscripcion);
    
    // Nuevas consultas para mejor funcionalidad
    List<CourseAttend> findByInscripcion(Inscription inscripcion);
    
    List<CourseAttend> findByInscripcionAndPresente(Inscription inscripcion, Boolean presente);
    
    Optional<CourseAttend> findByInscripcionAndFechaAsistencia(Inscription inscripcion, LocalDate fecha);
    
    @Query("SELECT ca FROM CourseAttend ca WHERE ca.inscripcion.user.id = :userId")
    List<CourseAttend> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT ca FROM CourseAttend ca WHERE ca.inscripcion.shift.id = :shiftId")
    List<CourseAttend> findByShiftId(@Param("shiftId") Long shiftId);
    
    @Query("SELECT ca FROM CourseAttend ca WHERE ca.inscripcion.shift.id = :shiftId AND ca.fechaAsistencia = :fecha")
    List<CourseAttend> findByShiftIdAndFecha(@Param("shiftId") Long shiftId, @Param("fecha") LocalDate fecha);
    
    @Query("SELECT COUNT(ca) FROM CourseAttend ca WHERE ca.inscripcion = :inscripcion AND ca.presente = true")
    long countByInscripcionAndPresenteTrue(@Param("inscripcion") Inscription inscripcion);
    
    @Query("SELECT ca FROM CourseAttend ca WHERE ca.fechaAsistencia BETWEEN :startDate AND :endDate")
    List<CourseAttend> findByFechaAsistenciaBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Para obtener el historial de asistencias de un usuario en un rango de fechas
    @Query("SELECT ca FROM CourseAttend ca WHERE ca.inscripcion.user.id = :userId AND ca.fechaAsistencia BETWEEN :startDate AND :endDate ORDER BY ca.fechaAsistencia DESC")
    List<CourseAttend> findUserAttendanceBetweenDates(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
package com.example.g7_back_mobile.repositories;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.g7_back_mobile.repositories.entities.CourseAttend;
import com.example.g7_back_mobile.repositories.entities.Inscription;

@Repository
public interface CourseAttendRepository extends JpaRepository<CourseAttend, Long> {
    boolean existsByInscripcionAndFechaAsistencia(Inscription inscripcion, LocalDate fecha);
    long countByInscripcion(Inscription inscripcion);

}
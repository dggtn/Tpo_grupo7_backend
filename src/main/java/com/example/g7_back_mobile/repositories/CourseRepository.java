package com.example.g7_back_mobile.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.g7_back_mobile.repositories.entities.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    Boolean existsByName(String name);
    Optional<Course> findByName(String name);

    List<Course> findByNameContainingIgnoreCase(String name);
    List<Course> findByFechaInicioBetween(LocalDate startDate, LocalDate endDate); // <-- CAMBIO AQUÍ
    List<Course> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
   
}
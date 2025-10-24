package com.example.g7_back_mobile.repositories;

import com.example.g7_back_mobile.repositories.entities.Course;
import com.example.g7_back_mobile.repositories.entities.Shift;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long>, JpaSpecificationExecutor<Shift> {

    List<Shift> findByClase(Course course);
    List<Shift> findByClaseId(Long courseId);
    List<Shift> findByDiaEnQueSeDicta(int dia);
    

}
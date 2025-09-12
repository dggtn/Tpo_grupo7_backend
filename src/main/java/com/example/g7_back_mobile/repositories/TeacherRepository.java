package com.example.g7_back_mobile.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.g7_back_mobile.repositories.entities.Teacher;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByName(String name);
   
    List<Teacher> findByNameContainingIgnoreCase(String name);
}

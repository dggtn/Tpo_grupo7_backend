package com.example.g7_back_mobile.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.g7_back_mobile.repositories.entities.Sport;

@Repository
public interface SportRepository extends JpaRepository<Sport, Long> {
    
}

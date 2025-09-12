package com.example.g7_back_mobile.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.g7_back_mobile.repositories.entities.Headquarter;

@Repository
public interface HeadquarterRepository extends JpaRepository<Headquarter, Long> {
    Optional<Headquarter> findByName(String name);
    Optional<Headquarter> findByEmail(String email);
    Optional<Headquarter> findByPhone(String phone);
    List<Headquarter> findByNameContainingIgnoreCase(String name);
  
}

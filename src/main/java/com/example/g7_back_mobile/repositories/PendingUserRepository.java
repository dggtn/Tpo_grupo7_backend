package com.example.g7_back_mobile.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.g7_back_mobile.repositories.entities.PendingUser;

@Repository
public interface PendingUserRepository extends JpaRepository<PendingUser, String> { // El ID es de tipo String (el email)
}
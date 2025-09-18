package com.example.g7_back_mobile.repositories;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.g7_back_mobile.repositories.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.username = ?1")
    Optional<User> findByUsername(String username);//
    Optional<User> findByEmail(String email);//
    //User findByEmail(String username);
    //User findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

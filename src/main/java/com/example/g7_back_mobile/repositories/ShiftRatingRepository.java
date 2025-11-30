package com.example.g7_back_mobile.repositories;

import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.repositories.entities.ShiftRating;
import com.example.g7_back_mobile.repositories.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShiftRatingRepository extends JpaRepository<ShiftRating, Long> {

    List<ShiftRating> findByUser(User user);
    Optional<ShiftRating> findByUserAndShift(User user, Shift shift);
}

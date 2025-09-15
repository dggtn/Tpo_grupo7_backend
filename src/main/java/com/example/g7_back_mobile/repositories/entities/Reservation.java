package com.example.g7_back_mobile.repositories.entities;

import java.time.LocalDateTime;

import com.example.g7_back_mobile.controllers.dtos.ReservationDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long idUser;
    private Long idShift;
    private MetodoDePago metodoDePago;
    private LocalDateTime expiryDate;

    public ReservationDTO toDTO(){
        return new ReservationDTO(
            this.id,
            this.idUser,
            this.idShift,
            this.metodoDePago,
            this.expiryDate
            
        );
    }
}

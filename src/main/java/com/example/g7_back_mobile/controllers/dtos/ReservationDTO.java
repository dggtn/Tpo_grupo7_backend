package com.example.g7_back_mobile.controllers.dtos;

import java.time.LocalDateTime;

import com.example.g7_back_mobile.repositories.entities.MetodoDePago;
import com.example.g7_back_mobile.repositories.entities.Reservation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDTO {
    private Long id;
    private Long idUser;
    private Long idShift;
    private MetodoDePago metodoDePago;
    private LocalDateTime expiryDate;

    public Reservation toEntity(){
        return new Reservation(
            this.id,
            this.idUser,
            this.idShift,
            this.metodoDePago,
            this.expiryDate
            
        );
    }
}

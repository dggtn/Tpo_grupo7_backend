package com.example.g7_back_mobile.controllers.dtos;

import com.example.g7_back_mobile.repositories.entities.MetodoDePago;

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
}

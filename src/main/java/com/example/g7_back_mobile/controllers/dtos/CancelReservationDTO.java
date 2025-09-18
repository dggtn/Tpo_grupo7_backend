package com.example.g7_back_mobile.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelReservationDTO {
    
    private Long idUser;
    private Long idShift;
    private String motivoCancelacion; // Opcional: para feedback del usuario
    
    @Override
    public String toString() {
        return String.format("CancelReservationDTO{idUser=%d, idShift=%d, motivo='%s'}", 
            idUser, idShift, motivoCancelacion);
    }
}
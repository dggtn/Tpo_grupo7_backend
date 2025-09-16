package com.example.g7_back_mobile.controllers.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsistenciaDTO {
    
    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser un número positivo")
    private Long idUser;
    
    @NotNull(message = "El ID del cronograma es obligatorio")
    @Positive(message = "El ID del cronograma debe ser un número positivo")
    private Long idCronograma;
    
    @Override
    public String toString() {
        return String.format("AsistenciaDTO{idUser=%d, idCronograma=%d}", idUser, idCronograma);
    }
}
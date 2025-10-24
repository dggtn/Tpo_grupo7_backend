package com.example.g7_back_mobile.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaseDTO {
    private Long idCurso;
    private Long idClase;
    private String sede;
    private String nombreClase;
    private String horario;
    private String tipoDeporte;
}

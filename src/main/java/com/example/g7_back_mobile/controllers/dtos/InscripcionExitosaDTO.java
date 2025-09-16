package com.example.g7_back_mobile.controllers.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InscripcionExitosaDTO {

    private Long idInscripcion;
    private String nombreCurso;
    private String emailUser;
    private LocalDateTime fechaInscripcion;
    private String estado;

    
}

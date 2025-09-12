package com.example.g7_back_mobile.controllers.dtos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsistenciaResultadoDTO {

    private int totalClases;
    private long clasesAsistidas;
    private String mensaje;
    
}

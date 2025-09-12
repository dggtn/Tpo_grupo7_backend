package com.example.g7_back_mobile.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateShiftRequest {
    private String horaInicio;
    private String horaFin;
    private int vacancy;
    private int diaEnQueSeDicta;
    
    
    
}

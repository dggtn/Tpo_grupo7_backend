package com.example.g7_back_mobile.controllers.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.example.g7_back_mobile.repositories.entities.CourseAttend;
import com.example.g7_back_mobile.repositories.entities.Inscription;
import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.repositories.entities.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InscriptionDTO {
    private Long id;

    private User user;
 
    private Shift shift;
    
    private LocalDateTime fechaInscripcion;
    
    private String estado; // ACTIVA, CANCELADA, FINALIZADA
    
    private List<CourseAttend> asistencias;

    public Inscription toEntity(){
        return new Inscription(
            this.id,
            this.user,
            this.shift,
            this.fechaInscripcion,
            this.estado,
            this.asistencias
            
        );
    }
}

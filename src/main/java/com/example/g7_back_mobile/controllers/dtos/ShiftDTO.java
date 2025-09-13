package com.example.g7_back_mobile.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.g7_back_mobile.repositories.entities.Course;
import com.example.g7_back_mobile.repositories.entities.Headquarter;
import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.repositories.entities.Teacher;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShiftDTO {
    private Long id;
    private String horaInicio;
    private String horaFin;
    private int diaEnQueSeDicta;
    private int vacancy;
    private Course clase;
    private Headquarter sede;
    private Teacher teacher;

     public Shift toEntity() {
    return new Shift(
        this.id,
        this.horaInicio,
        this.horaFin,
        this.diaEnQueSeDicta,
        this.vacancy,
        this.clase, //??????
        this.sede,
        this.teacher
        
    );
}
   

}
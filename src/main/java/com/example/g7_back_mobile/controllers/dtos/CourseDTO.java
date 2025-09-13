package com.example.g7_back_mobile.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

import com.example.g7_back_mobile.repositories.entities.Course;
import com.example.g7_back_mobile.repositories.entities.Headquarter;
import com.example.g7_back_mobile.repositories.entities.Sport;
import com.example.g7_back_mobile.repositories.entities.Teacher;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String name;
    private Sport sportName;
    private String fechaInicio;
    private String fechaFin;
    private int length;
    private double price;
    private String imgCourse;
    @JsonIgnore
    private List<Headquarter> sedes; //sedes en las que se dicta la clase
    private List<Teacher> teachers; 
    private List<ShiftDTO> shifts;

    public Course toEntity() {
        return Course.builder()
                .id(this.id)
                .name(this.name)
                .sportName(this.sportName)
                .fechaInicio(LocalDate.parse(this.fechaInicio)) // Convertimos el String a LocalDate
                .fechaFin(LocalDate.parse(this.fechaFin))       // Convertimos el String a LocalDate
                .length(this.length)
                .price(this.price)
                .imgCourse(this.imgCourse)
                .sedes(this.sedes)
                .teachers(this.teachers)
                .build();
    }

    
}
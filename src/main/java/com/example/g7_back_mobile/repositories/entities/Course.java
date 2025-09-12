package com.example.g7_back_mobile.repositories.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.g7_back_mobile.controllers.dtos.CourseDTO;
import com.example.g7_back_mobile.controllers.dtos.ShiftDTO;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int length;
    private double price;
    @ManyToMany
    @JoinTable(
    name = "headquarter_course", 
    joinColumns = @JoinColumn(name = "course"), 
    inverseJoinColumns = @JoinColumn(name = "headquarter_id"))
    private List<Headquarter> sedes; //sedes en las que se dicta la clase
    @ManyToMany
    @JoinTable(
    name = "teacher_course", 
    joinColumns = @JoinColumn(name = "course"), 
    inverseJoinColumns = @JoinColumn(name = "teacher_id"))
    private List<Teacher> teachers; //profesores que dictan la clase
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    private List<Shift> shifts = new ArrayList<>();//turnos disponibles de la clase

    public CourseDTO toDTO(){
        List<ShiftDTO> shiftDTO = this.shifts
                .stream()
                .map(Shift::toDTO)
                .collect(Collectors.toList());
        return new CourseDTO(
            this.id,
            this.name,
            this.fechaInicio.toString(),
            this.fechaFin.toString(),
            this.length,
            this.price,
            this.sedes,
            this.teachers,
            shiftDTO
        );
    }
}

package com.example.g7_back_mobile.repositories.entities;
import java.util.List;

import com.example.g7_back_mobile.controllers.dtos.ShiftDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    @Column(name = "hora_inicio", nullable = false)
    private String horaInicio;
    @Column(name = "hora_fin", nullable = false)
    private String horaFin;
    private int diaEnQueSeDicta;
    private int vacancy;
    @ManyToOne
    @JoinColumn(nullable = false, name = "course_id")
    @JsonBackReference
    private Course clase;
    @ManyToOne
    @JoinColumn(name = "headquarter_id")
    private Headquarter sede;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "teacher_id")
    @JsonManagedReference
    private Teacher teacher;
    @ManyToMany
    @JoinTable(
    name = "headquarter_course", 
    joinColumns = @JoinColumn(name = "course"), 
    inverseJoinColumns = @JoinColumn(name = "sport_id"))
    private List<Sport> sports; //funciona como "categoria": los tipos de deporte que hay en un turno

    public ShiftDTO toDTO() {
    return new ShiftDTO(
        this.id,
        this.description,
        this.horaInicio,
        this.horaFin,
        this.diaEnQueSeDicta,
        this.vacancy,
        this.clase, //??????
        this.sede,
        this.teacher,
        this.sports
        
    );
}
}

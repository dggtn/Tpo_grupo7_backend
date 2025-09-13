package com.example.g7_back_mobile.repositories.entities;

import com.example.g7_back_mobile.controllers.dtos.ShiftDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    @Column(name = "hora_inicio", nullable = false)
    private String horaInicio;
    @Column(name = "hora_fin", nullable = false)
    private String horaFin;
    private int diaEnQueSeDicta;
    private int vacancy;
    @ManyToOne
    @JoinColumn(nullable = false, name = "clase_id")
    @JsonBackReference
    private Course clase;
    @ManyToOne
    @JoinColumn(name = "headquarter_id")
    private Headquarter sede;
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    public ShiftDTO toDTO() {
    return new ShiftDTO(
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

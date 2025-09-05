package com.example.g7_back_mobile.repositories.entities;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private LocalDateTime localTime;
    @ManyToOne
    @JoinColumn(nullable = false, name = "clase_id")
    @JsonBackReference
    private Class clase;
    @ManyToOne
    @JoinColumn(name = "headquarter_id")
    private Headquarter sede;
    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Teacher> teachers;
    @ManyToOne
    @JoinColumn(name = "sportType_id")
    private Sport sportType; //funciona como "categoria": los tipos de deporte que hay en un turno
}

package com.example.g7_back_mobile.repositories.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
public class Class {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDateTime dateTime;
    private double price;
    @ManyToMany
    @JoinTable(
    name = "headquarter_class", 
    joinColumns = @JoinColumn(name = "class"), 
    inverseJoinColumns = @JoinColumn(name = "headquarter_id"))
    private List<Headquarter> sedes; //sedes en las que se dicta la clase
    @OneToMany(mappedBy = "clase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    private List<Shift> shifts = new ArrayList<>();//turnos disponibles de la clase
}

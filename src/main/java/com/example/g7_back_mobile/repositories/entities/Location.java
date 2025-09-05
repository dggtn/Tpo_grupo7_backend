package com.example.g7_back_mobile.repositories.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double lenght;
    private Double latitude;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sede_id")
    @JsonBackReference
    private Headquarter sede;
}

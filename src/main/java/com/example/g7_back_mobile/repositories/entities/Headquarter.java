package com.example.g7_back_mobile.repositories.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
public class Headquarter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String phone;
    private String address;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String whattsapp;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id")
    @JsonManagedReference
    private Location location;
}

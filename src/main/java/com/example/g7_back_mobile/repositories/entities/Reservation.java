package com.example.g7_back_mobile.repositories.entities;

import java.time.LocalDateTime;

import com.example.g7_back_mobile.controllers.dtos.ReservationDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.g7_back_mobile.repositories.entities.EstadoReserva;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long idUser;
    private Long idShift;
    @Enumerated(EnumType.ORDINAL) 
    private MetodoDePago metodoDePago;
    private LocalDateTime expiryDate;
    @Enumerated(EnumType.STRING)         
    private EstadoReserva status; 
    private Boolean attended = false;
    private LocalDateTime attendedAt;

	public com.example.g7_back_mobile.controllers.dtos.ReservationDTO toDTO() {
		return com.example.g7_back_mobile.controllers.dtos.ReservationDTO.fromEntity(this);
	}
	
}

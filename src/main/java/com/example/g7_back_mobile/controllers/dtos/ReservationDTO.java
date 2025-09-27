package com.example.g7_back_mobile.controllers.dtos;

import java.time.LocalDateTime;

import com.example.g7_back_mobile.repositories.entities.EstadoReserva;
import com.example.g7_back_mobile.repositories.entities.MetodoDePago;
import com.example.g7_back_mobile.repositories.entities.Reservation;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReservationDTO {
    private Long id;
    private Long idUser;
    private Long idShift;
    private MetodoDePago metodoDePago;     // ← si tu entidad lo tiene
    private LocalDateTime expiryDate;
    private EstadoReserva status;          // ← ahora el DTO también lo tiene

    // Mapper desde entidad → DTO
    public static ReservationDTO fromEntity(Reservation r) {
        ReservationDTO dto = new ReservationDTO();
        if (r == null) return dto;
        dto.setId(r.getId());
        dto.setIdUser(r.getIdUser());
        dto.setIdShift(r.getIdShift());
        dto.setMetodoDePago(r.getMetodoDePago());
        dto.setExpiryDate(r.getExpiryDate());
        dto.setStatus(r.getStatus());
        return dto;
    }

    // Mapper DTO → entidad (¡evitá constructores largos!)
    public Reservation toEntity() {
        return Reservation.builder()
                .id(this.id)
                .idUser(this.idUser)
                .idShift(this.idShift)
                .metodoDePago(this.metodoDePago)
                .expiryDate(this.expiryDate)
                .status(this.status) // el service suele setear ACTIVA al crear
                .build();
    }
}

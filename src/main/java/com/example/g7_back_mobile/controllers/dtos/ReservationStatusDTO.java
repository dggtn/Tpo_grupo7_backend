package com.example.g7_back_mobile.controllers.dtos;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationStatusDTO {
    
    private Long reservationId;
    private String nombreCurso;
	private Long shiftId;
    private String diaClase;
    private String horaClase;
    private LocalDateTime fechaExpiracion;
    private boolean puedeInscribirse;
    private boolean puedeCancelarse;
    private long minutosParaExpirar;
    private long minutosParaPrimeraClase;
    private String estadoReserva;         
    private boolean cancelable; 
	
    public ReservationStatusDTO(ReservationDTO reservation) {
        this.reservationId = reservation.getId();
        // Aquí se pueden agregar más campos calculados
    }
    
    /**
     * Calcula si la reserva puede ser cancelada (1 hora antes de primera clase)
     */
    public void calcularEstadoCancelacion() {
        LocalDateTime ahora = LocalDateTime.now();
        
        if (fechaExpiracion != null) {
            this.puedeCancelarse = ahora.isBefore(fechaExpiracion);
            
            if (puedeCancelarse) {
                this.minutosParaExpirar = java.time.Duration.between(ahora, fechaExpiracion).toMinutes();
                
                if (minutosParaExpirar <= 120) { // 2 horas
                    this.estadoReserva = "PROXIMA_A_EXPIRAR";
                } else {
                    this.estadoReserva = "ACTIVA";
                }
            } else {
                this.estadoReserva = "EXPIRADA";
                this.minutosParaExpirar = 0;
            }
        }
        
        // La inscripción siempre es posible hasta que expire
        this.puedeInscribirse = this.puedeCancelarse;
    }
}

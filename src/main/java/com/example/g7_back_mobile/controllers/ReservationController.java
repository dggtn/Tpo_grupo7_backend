package com.example.g7_back_mobile.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.g7_back_mobile.controllers.dtos.ReservationDTO;
import com.example.g7_back_mobile.controllers.dtos.ResponseData;
import com.example.g7_back_mobile.repositories.entities.Reservation;
import com.example.g7_back_mobile.repositories.entities.User;
import com.example.g7_back_mobile.services.ReserveService;
import com.example.g7_back_mobile.services.UserService;
import com.example.g7_back_mobile.services.exceptions.UserException;
import com.example.g7_back_mobile.repositories.entities.MetodoDePago;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private UserService userService;
    @Autowired
    private ReserveService reservationService;

    @GetMapping("")
    public ResponseEntity<ResponseData<?>> getUserReservations(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[ReservationController.getUserReservations] Consultando reservas para usuario: " + userDetails.getUsername());
            
            // CAMBIO: Usar getUserByEmail en lugar de getUserByUsername
            User authUser = userService.getUserByEmail(userDetails.getUsername());

            List<ReservationDTO> reservations = reservationService.getUserReservations(authUser.getId())
                    .stream().map(Reservation::toDTO).toList();
            
            System.out.println("[ReservationController.getUserReservations] Encontradas " + reservations.size() + " reservas activas");

            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(reservations));

        } catch (UserException error) {
            System.err.println("[ReservationController.getUserReservations] Error de usuario: " + error.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseData.error(error.getMessage()));
        } catch (Exception error) {
            System.err.println("[ReservationController.getUserReservations] Error inesperado: " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("No se pudieron obtener las reservas."));
        }
    }

	@PostMapping("/reservar")
	public ResponseEntity<ResponseData<?>> reserveCourse(
			@RequestBody ReservationDTO reservationDTO,
			@AuthenticationPrincipal UserDetails userDetails) {
		try {
			System.out.println("[ReservationController.reserveCourse] Procesando nueva reserva: " + reservationDTO);
	
			// 1) Validar shiftId (lo único que debe venir del body)
			if (reservationDTO.getIdShift() == null) {
				return ResponseEntity.badRequest()
					.body(ResponseData.error("El ID del turno es obligatorio."));
			}
	
			// 2) Obtener el usuario autenticado desde el JWT
			if (userDetails == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ResponseData.error("No autenticado"));
			}
	
			// En tu proyecto el username del token es el email:
			User authUser = userService.getUserByEmail(userDetails.getUsername());
			if (authUser == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ResponseData.error("Usuario no encontrado"));
			}
	
			// 3) Forzar el idUser desde el contexto (ignorar lo que venga en el body)
			reservationDTO.setIdUser(authUser.getId());
	
			// (Opcional) valor por defecto para método de pago si es requerido por la lógica
			if (reservationDTO.getMetodoDePago() == null) {
				reservationDTO.setMetodoDePago(MetodoDePago.DEBIT_CARD); 
			}
	
			// 4) Ejecutar la reserva
			reservationService.reserveClass(reservationDTO);
	
			System.out.println("[ReservationController.reserveCourse] Reserva creada exitosamente");
			return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseData.success("Curso reservado exitosamente! La reserva expira 1 hora antes de la primera clase."));
	
		} catch (IllegalStateException | IllegalArgumentException e) {
			System.err.println("[ReservationController.reserveCourse] Error de validación: " + e.getMessage());
			return ResponseEntity.badRequest().body(ResponseData.error(e.getMessage()));
		} catch (Exception e) {
			System.err.println("[ReservationController.reserveCourse] Error inesperado: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ResponseData.error("Ocurrió un error inesperado al reservar el curso."));
		}
	}


    @DeleteMapping("/cancelar/{shiftId}")
    public ResponseEntity<ResponseData<?>> cancelReservation(
            @PathVariable Long shiftId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[ReservationController.cancelReservation] Cancelando reserva para shift: " + shiftId + ", usuario: " + userDetails.getUsername());
            
            // Validaciones básicas
            if (shiftId == null) {
                return ResponseEntity.badRequest()
                    .body(ResponseData.error("El ID del turno es obligatorio."));
            }
            
            // Obtener usuario autenticado
            User authUser = userService.getUserByEmail(userDetails.getUsername());
            
            // Cancelar la reserva
            reservationService.cancelReservation(authUser.getId(), shiftId);
            
            System.out.println("[ReservationController.cancelReservation] Reserva cancelada exitosamente");
            return ResponseEntity.ok().body(ResponseData.success("Reserva cancelada exitosamente. Tu cupo ha sido liberado."));
            
        } catch (IllegalArgumentException e) {
            System.err.println("[ReservationController.cancelReservation] Error de validación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseData.error(e.getMessage()));
            
        } catch (IllegalStateException e) {
            System.err.println("[ReservationController.cancelReservation] Error de estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseData.error(e.getMessage()));
            
        } catch (UserException e) {
            System.err.println("[ReservationController.cancelReservation] Error de usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseData.error(e.getMessage()));
            
        } catch (Exception e) {
            System.err.println("[ReservationController.cancelReservation] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("Ocurrió un error inesperado al cancelar la reserva."));
        }
    }

    @DeleteMapping("/cancelar_por_id/{reservationId}")
    public ResponseEntity<ResponseData<?>> cancelReservationById(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[ReservationController.cancelReservationById] Cancelando reserva ID: " + reservationId);
            
            // Esta implementación requeriría un método adicional en el servicio
            // que busque la reserva por ID y verifique que pertenece al usuario autenticado
            
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ResponseData.error("Funcionalidad en desarrollo. Use /cancelar/{shiftId} en su lugar."));
                
        } catch (Exception e) {
            System.err.println("[ReservationController.cancelReservationById] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("Error procesando la cancelación."));
        }
    }
}
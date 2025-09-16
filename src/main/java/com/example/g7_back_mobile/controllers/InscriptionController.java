package com.example.g7_back_mobile.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.g7_back_mobile.controllers.dtos.InscripcionExitosaDTO;
import com.example.g7_back_mobile.controllers.dtos.InscriptionDTO;
import com.example.g7_back_mobile.controllers.dtos.ReservationDTO;
import com.example.g7_back_mobile.controllers.dtos.ResponseData;
import com.example.g7_back_mobile.repositories.entities.Inscription;
import com.example.g7_back_mobile.repositories.entities.User;
import com.example.g7_back_mobile.services.InscriptionService;
import com.example.g7_back_mobile.services.UserService;
import com.example.g7_back_mobile.services.exceptions.UserException;

@RestController
@RequestMapping("/inscriptions")
public class InscriptionController {

    @Autowired
    private InscriptionService inscriptionService;
    @Autowired
    private UserService userService;
    
    @PostMapping("/inscribir")
    public ResponseEntity<ResponseData<?>> enrollUser(@RequestBody ReservationDTO reservationDTO) {
        try {
            System.out.println("[InscriptionController.enrollUser] Recibiendo petición de inscripción: " + reservationDTO);
            
            // Validaciones básicas
            if (reservationDTO.getIdUser() == null) {
                return ResponseEntity.badRequest()
                    .body(ResponseData.error("El ID del usuario es obligatorio."));
            }
            
            if (reservationDTO.getIdShift() == null) {
                return ResponseEntity.badRequest()
                    .body(ResponseData.error("El ID del turno es obligatorio."));
            }
            
            if (reservationDTO.getMetodoDePago() == null) {
                return ResponseEntity.badRequest()
                    .body(ResponseData.error("El método de pago es obligatorio."));
            }

            InscripcionExitosaDTO resultado = inscriptionService.enrollUser(reservationDTO);
            
            System.out.println("[InscriptionController.enrollUser] Inscripción exitosa con ID: " + resultado.getIdInscripcion());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseData.success(resultado));
                
        } catch (IllegalArgumentException e) {
            System.err.println("[InscriptionController.enrollUser] Error de validación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseData.error("Error de validación: " + e.getMessage()));
                
        } catch (IllegalStateException e) {
            System.err.println("[InscriptionController.enrollUser] Error de estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseData.error("Error de estado: " + e.getMessage()));
                
        } catch (UserException e) {
            System.err.println("[InscriptionController.enrollUser] Error de usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseData.error(e.getMessage()));
                
        } catch (Exception e) {
            System.err.println("[InscriptionController.enrollUser] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("Ocurrió un error inesperado al inscribir. Por favor, intente nuevamente."));
        }
    }

    @PostMapping("/inscribir_reserva")
    public ResponseEntity<ResponseData<?>> enrollWithReservation(@RequestBody ReservationDTO reservationDTO) {
        try {
            System.out.println("[InscriptionController.enrollWithReservation] Recibiendo petición de inscripción con reserva: " + reservationDTO);
            
            // Validaciones básicas
            if (reservationDTO.getIdUser() == null) {
                return ResponseEntity.badRequest()
                    .body(ResponseData.error("El ID del usuario es obligatorio."));
            }
            
            if (reservationDTO.getIdShift() == null) {
                return ResponseEntity.badRequest()
                    .body(ResponseData.error("El ID del turno es obligatorio."));
            }

            InscripcionExitosaDTO resultado = inscriptionService.enrollWithReservation(reservationDTO);
            
            System.out.println("[InscriptionController.enrollWithReservation] Inscripción con reserva exitosa con ID: " + resultado.getIdInscripcion());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseData.success(resultado));
                
        } catch (IllegalArgumentException e) {
            System.err.println("[InscriptionController.enrollWithReservation] Error de validación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseData.error("Error de validación: " + e.getMessage()));
                
        } catch (IllegalStateException e) {
            System.err.println("[InscriptionController.enrollWithReservation] Error de estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseData.error("Error de estado: " + e.getMessage()));
                
        } catch (UserException e) {
            System.err.println("[InscriptionController.enrollWithReservation] Error de usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseData.error(e.getMessage()));
                
        } catch (Exception e) {
            System.err.println("[InscriptionController.enrollWithReservation] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("Ocurrió un error inesperado al inscribir. Por favor, intente nuevamente."));
        }
    }

    @GetMapping("/by-user")
    public ResponseEntity<ResponseData<?>> getInscriptionsByUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[InscriptionController.getInscriptionsByUser] Consultando inscripciones para usuario: " + userDetails.getUsername());
            
            //Usar getUserByEmail en lugar de getUserByUsername
            User authUser = userService.getUserByEmail(userDetails.getUsername());
            List<InscriptionDTO> inscriptions = inscriptionService.getUserInscriptions(authUser.getId())
                    .stream().map(Inscription::toDTO).toList();
            
            System.out.println("[InscriptionController.getInscriptionsByUser] Encontradas " + inscriptions.size() + " inscripciones");
            
            return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseData.success(inscriptions));
                
        } catch (UserException error) {
            System.err.println("[InscriptionController.getInscriptionsByUser] Error de usuario: " + error.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseData.error(error.getMessage()));
                
        } catch (Exception error) {
            System.err.println("[InscriptionController.getInscriptionsByUser] Error inesperado: " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("No se pudieron obtener las inscripciones. Por favor, intente nuevamente."));
        }
    }
}
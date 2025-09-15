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

            InscripcionExitosaDTO resultado = inscriptionService.enrollUser(reservationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(ResponseData.success(resultado));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResponseData.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("Ocurrió un error inesperado al inscribir."));
        }
    }

    @PostMapping("/inscribir_reserva")
    public ResponseEntity<ResponseData<?>> enrollWithReservation(@RequestBody ReservationDTO reservationDTO) {
        try {

            InscripcionExitosaDTO resultado = inscriptionService.enrollWithReservation(reservationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(ResponseData.success(resultado));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResponseData.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("Ocurrió un error inesperado al inscribir."));
        }
    }

    @GetMapping("/by-user")
    public ResponseEntity<ResponseData<?>> getInscriptionsByUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            // CAMBIO: Usar getUserByEmail en lugar de getUserByUsername
            User authUser = userService.getUserByEmail(userDetails.getUsername());
            List<InscriptionDTO> inscriptions = inscriptionService.getUserInscriptions(authUser.getId())
                    .stream().map(Inscription::toDTO).toList();
            
            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(inscriptions));
        } catch (UserException error) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ResponseData.error(error.getMessage()));
        } catch (Exception error) {
            System.out.printf("[InscriptionController.getInscriptionsByUser] -> %s", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("No se pudieron obtener las inscripciones."));
        }
    }
}

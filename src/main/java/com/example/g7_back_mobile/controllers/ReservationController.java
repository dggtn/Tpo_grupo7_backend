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

import com.example.g7_back_mobile.controllers.dtos.ReservationDTO;
import com.example.g7_back_mobile.controllers.dtos.ResponseData;
import com.example.g7_back_mobile.repositories.entities.Reservation;
import com.example.g7_back_mobile.repositories.entities.User;
import com.example.g7_back_mobile.services.ReserveService;
import com.example.g7_back_mobile.services.UserService;
import com.example.g7_back_mobile.services.exceptions.UserException;

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
            // CAMBIO: Usar getUserByEmail en lugar de getUserByUsername
            User authUser = userService.getUserByEmail(userDetails.getUsername());

            List<ReservationDTO> reservations = reservationService.getUserReservations(authUser.getId())
                    .stream().map(Reservation::toDTO).toList();

            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(reservations));

        } catch (UserException error) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ResponseData.error(error.getMessage()));
        } catch (Exception error) {
            System.out.printf("[ReservationController.getUserReservations] -> %s", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("No se pudieron obtener las reservas."));
        }
    }

    @PostMapping("/reservar")
    public ResponseEntity<ResponseData<?>> reserveCourse(@RequestBody ReservationDTO reservationDTO) {
        try {

            reservationService.reserveClass(reservationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(ResponseData.success("Curso reservado!"));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResponseData.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("Ocurrió un error inesperado al reservar el curso."));
        }
    }


        
}

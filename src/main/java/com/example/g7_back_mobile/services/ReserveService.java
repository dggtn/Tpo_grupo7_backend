package com.example.g7_back_mobile.services;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.g7_back_mobile.controllers.dtos.ReservationDTO;
import com.example.g7_back_mobile.repositories.InscriptionRepository;
import com.example.g7_back_mobile.repositories.ReservationRepository;
import com.example.g7_back_mobile.repositories.ShiftRepository;
import com.example.g7_back_mobile.repositories.UserRepository;
import com.example.g7_back_mobile.repositories.entities.Reservation;
import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.repositories.entities.User;

import jakarta.transaction.Transactional;

@Service
public class ReserveService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShiftRepository shiftRepository;
    @Autowired
    private InscriptionRepository inscripcionRepository;
	@Autowired
	private ReservationRepository reservationRepository;

    public List<Reservation> getUserReservations(Long userId) throws Exception {
      try {
        return reservationRepository.findByIdUser(userId);
      } catch (Exception error) {
        throw new Exception("[ReservationService.getUserReservations] -> " + error.getMessage());
      }
    }
    
    @Transactional
	public void reserveClass(ReservationDTO reservationDTO){

		// 1. BÚSQUEDA DE ENTIDADES
		User user = userRepository.findById(reservationDTO.getIdUser())
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + reservationDTO.getIdUser()));

		Shift courseSchedule = shiftRepository.findById(reservationDTO.getIdShift())
				.orElseThrow(() -> new IllegalArgumentException("Cronograma no encontrado con ID: " + reservationDTO.getIdShift()));

		// 2. VALIDACIONES DE NEGOCIO
		if (courseSchedule.getVacancy() <= 0) {
			throw new IllegalStateException("No quedan cupos disponibles para este curso.");
		}
		inscripcionRepository.findByUserAndShift(user, courseSchedule)
			.ifPresent(inscripcion -> {
				if ("ACTIVA".equals(inscripcion.getEstado())) {
					throw new IllegalArgumentException("El usuario ya está inscripto y activo en este curso.");
				}
			});
		
		// 4. CREACIÓN DE LA RESERVA
		Reservation nuevaReservation = Reservation.builder()
				
				.idUser(user.getId())
				.idShift(courseSchedule.getId())
				.expiryDate(LocalDateTime.now().plusHours(48))
				.build();
		
		reservationRepository.save(nuevaReservation);

		// 5. ACTUALIZACIÓN DE VACANTES
		courseSchedule.setVacancy(courseSchedule.getVacancy() - 1);
		shiftRepository.save(courseSchedule);
	
	}
    
}

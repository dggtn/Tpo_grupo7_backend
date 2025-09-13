package com.example.g7_back_mobile.services;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.g7_back_mobile.controllers.dtos.InscripcionExitosaDTO;
import com.example.g7_back_mobile.controllers.dtos.ReservationDTO;
import com.example.g7_back_mobile.repositories.InscriptionRepository;
import com.example.g7_back_mobile.repositories.ReservationRepository;
import com.example.g7_back_mobile.repositories.ShiftRepository;
import com.example.g7_back_mobile.repositories.UserRepository;
import com.example.g7_back_mobile.repositories.entities.Course;
import com.example.g7_back_mobile.repositories.entities.Inscription;
import com.example.g7_back_mobile.repositories.entities.MetodoDePago;
import com.example.g7_back_mobile.repositories.entities.Reservation;
import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.repositories.entities.User;
import com.example.g7_back_mobile.services.exceptions.UserException;

import jakarta.transaction.Transactional;

@Service
public class InscriptionService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShiftRepository shiftRepository;
    @Autowired
    private InscriptionRepository inscripcionRepository;
	@Autowired
	private ReservationRepository reservationRepository;
    @Autowired
    private EmailService emailService;

	@Transactional
	public InscripcionExitosaDTO enrollWithReservation(ReservationDTO reservationDTO){
		// 1. EXISTE UNA RESERVA 
		Reservation reservation = reservationRepository.findById(reservationDTO.getIdUser())
				.orElseThrow(() -> new UserException("No se encontró un reserva para este usuario. Puede que haya expirado."));

		if(reservation.getExpiryDate().isBefore(LocalDateTime.now())){
			reservationRepository.delete(reservation);
			throw new UserException("El tiempo de reserva ha expirado.");
		}

		Shift courseSchedule = shiftRepository.findById(reservationDTO.getIdShift())
				.orElseThrow(() -> new IllegalArgumentException("Cronograma no encontrado con ID: " + reservationDTO.getIdShift()));

		//2.ELIMINO LA RESERVA Y REESTABLEZCO LA VACANTE
		if(reservation != null){
		   reservationRepository.delete(reservation);
		   courseSchedule.setVacancy(courseSchedule.getVacancy() + 1);
		   shiftRepository.save(courseSchedule);
		}
		//3. REALIZO LA INSCRIPCION
		InscripcionExitosaDTO inscripcionExitosaDTO = enrollUser(reservationDTO);
		return inscripcionExitosaDTO;

	}

    @Transactional
	public InscripcionExitosaDTO enrollUser(ReservationDTO reservationDTO) {

		// 1. BÚSQUEDA DE ENTIDADES
		User user = userRepository.findById(reservationDTO.getIdUser())
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + reservationDTO.getIdUser()));

		Shift courseSchedule = shiftRepository.findById(reservationDTO.getIdShift())
				.orElseThrow(() -> new IllegalArgumentException("Cronograma no encontrado con ID: " + reservationDTO.getIdShift()));
		
		Course clase = courseSchedule.getClase();

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
        
		// 3. LÓGICA DE PROCESAMIENTO DE PAGO
		if (reservationDTO.getMetodoDePago() == MetodoDePago.CREDIT_CARD || reservationDTO.getMetodoDePago() == MetodoDePago.DEBIT_CARD) {
			double precioCurso = clase.getPrice();

		} else { 
			throw new IllegalStateException("Método de pago incorrecto.");
		}
		
		// 4. CREACIÓN DE LA INSCRIPCIÓN
		Inscription nuevaInscripcion = Inscription.builder()
				.user(user)
				.shift(courseSchedule)
				.fechaInscripcion(LocalDateTime.now())
				.estado("ACTIVA")
				.build();
		Inscription savedInscripcion = inscripcionRepository.save(nuevaInscripcion);

		// 5. ACTUALIZACIÓN DE VACANTES
		courseSchedule.setVacancy(courseSchedule.getVacancy() - 1);
		shiftRepository.save(courseSchedule);
		
		// 6. ENVÍO DE EMAIL DE CONFIRMACIÓN
		try {
			String subject = "¡Confirmación de tu inscripción al curso: " + clase.getName() + "!";
			String precioFormateado = String.format("$%d", clase.getPrice());

			String body = String.format(
				"Hola %s,\n\n" +
				"¡Te has inscrito exitosamente! Aquí están los detalles de tu curso:\n\n" +
				"--------------------------------------------------\n" +
				"Curso: %s\n" +
				"Descripción: %s\n" +
				"Instructor: %s\n" +
				"Duración: %d semanas\n" +
				"Costo: %s\n" +
				"--------------------------------------------------\n\n" +
				"Detalles del Horario:\n" +
				"Sede: %s (%s)\n" +
				"Día: %s\n" +
				"Horario: de %s a %s hs.\n\n" +
				"¡Te esperamos!",
				user.getFirstName(),
				clase.getName(),
				courseSchedule.getTeacher(),
				clase.getLength(),
				precioFormateado,
				courseSchedule.getSede().getName(),
				courseSchedule.getSede().getAddress(),
				"Día " + courseSchedule.getDiaEnQueSeDicta(),
				courseSchedule.getHoraInicio(),
				courseSchedule.getHoraFin()
			);
			emailService.sendEmail(user.getEmail(), subject, body);
		} catch (Exception e) {
			System.err.println("Error al enviar email de confirmación para inscripción ID: " + savedInscripcion.getId() + " - " + e.getMessage());
		}

		// 8. DEVOLVER RESPUESTA
		return new InscripcionExitosaDTO(
				savedInscripcion.getId(),
				savedInscripcion.getShift().getClase().getName(),
				savedInscripcion.getUser().getFirstName(),
				savedInscripcion.getUser().getEmail(),
				savedInscripcion.getFechaInscripcion(),
				savedInscripcion.getEstado()
		);

	
	}
    
    public List<Inscription> getUserInscriptions(Long studentId) throws Exception {
        try{
		return inscripcionRepository.findByUserId(studentId);
	} catch(Exception error){
		throw new Exception("[InscriptionService.getUserInscriptions] -> " + error.getMessage());
	}
             
    }

}

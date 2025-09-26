package com.example.g7_back_mobile.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.g7_back_mobile.controllers.dtos.ReservationDTO;
import com.example.g7_back_mobile.controllers.dtos.ReservationStatusDTO;
import com.example.g7_back_mobile.repositories.InscriptionRepository;
import com.example.g7_back_mobile.repositories.ReservationRepository;
import com.example.g7_back_mobile.repositories.ShiftRepository;
import com.example.g7_back_mobile.repositories.UserRepository;
import com.example.g7_back_mobile.repositories.entities.Course;
import com.example.g7_back_mobile.repositories.entities.Reservation;
import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.repositories.entities.User;

import jakarta.transaction.Transactional;

@Service
public class ReserveService {
    @Autowired
    private UserRepository userRepository;
	@Autowired
    private EmailService emailService;
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
    
    /**
     * Obtiene las reservas del usuario con información de estado adicional
     */
    public List<ReservationStatusDTO> getUserReservationsWithStatus(Long userId) throws Exception {
        try {
            List<Reservation> reservations = reservationRepository.findByIdUser(userId);
            
            return reservations.stream().map(reservation -> {
                try {
                    Shift shift = shiftRepository.findById(reservation.getIdShift()).orElse(null);
                    if (shift == null) return null;
                    
                    ReservationStatusDTO statusDTO = new ReservationStatusDTO();
                    statusDTO.setReservationId(reservation.getId());
                    statusDTO.setNombreCurso(shift.getClase().getName());
                    statusDTO.setDiaClase(obtenerNombreDia(shift.getDiaEnQueSeDicta()));
                    statusDTO.setHoraClase(shift.getHoraInicio());
                    statusDTO.setFechaExpiracion(reservation.getExpiryDate());
                    statusDTO.calcularEstadoCancelacion();
                    
                    return statusDTO;
                } catch (Exception e) {
                    System.err.println("[ReserveService.getUserReservationsWithStatus] Error procesando reserva: " + e.getMessage());
                    return null;
                }
            })
            .filter(dto -> dto != null)
            .toList();
            
        } catch (Exception error) {
            throw new Exception("[ReserveService.getUserReservationsWithStatus] -> " + error.getMessage());
        }
    }
    
    @Transactional
    public void cancelReservation(Long userId, Long shiftId) {
        System.out.println("[ReserveService.cancelReservation] Cancelando reserva para usuario: " + userId + ", shift: " + shiftId);
        
        // 1. VERIFICAR QUE EL USUARIO EXISTE
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));
        
        // 2. VERIFICAR QUE EL SHIFT EXISTE
        Shift shift = shiftRepository.findById(shiftId)
            .orElseThrow(() -> new IllegalArgumentException("Cronograma no encontrado con ID: " + shiftId));
        
        // 3. BUSCAR LA RESERVA
        Reservation reservation = reservationRepository.findByIdUserAndIdShift(userId, shiftId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró una reserva activa para este usuario y cronograma."));
        
        // 4. VERIFICAR QUE AÚN SE PUEDE CANCELAR (1 hora antes de la primera clase)
        LocalDateTime tiempoLimiteCancelacion = calcularTiempoLimiteReserva(shift);
        LocalDateTime ahora = LocalDateTime.now();
        
        if (ahora.isAfter(tiempoLimiteCancelacion)) {
            throw new IllegalStateException("Ya no es posible cancelar la reserva. El límite de cancelación ha expirado (1 hora antes de la primera clase).");
        }
        
        // 5. ELIMINAR LA RESERVA
        reservationRepository.delete(reservation);
        
        // 6. RESTAURAR LA VACANTE
        shift.setVacancy(shift.getVacancy() + 1);
        shiftRepository.save(shift);
        
        System.out.println("[ReserveService.cancelReservation] Reserva cancelada exitosamente. Vacante restaurada.");
        
        // 7. ENVIAR EMAIL DE CONFIRMACIÓN DE CANCELACIÓN
        try {
            enviarEmailCancelacionReserva(user, shift.getClase(), shift);
        } catch (Exception e) {
            System.err.println("[ReserveService.cancelReservation] Error enviando email de cancelación: " + e.getMessage());
            // No fallar la cancelación por un error de email
        }
    }
    
    @Transactional
	public void reserveClass(ReservationDTO reservationDTO){
        System.out.println("[ReserveService.reserveClass] Iniciando reserva para usuario: " + reservationDTO.getIdUser());

		// 1. BÚSQUEDA DE ENTIDADES
		User user = userRepository.findById(reservationDTO.getIdUser())
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + reservationDTO.getIdUser()));

		Shift courseSchedule = shiftRepository.findById(reservationDTO.getIdShift())
				.orElseThrow(() -> new IllegalArgumentException("Cronograma no encontrado con ID: " + reservationDTO.getIdShift()));

		// 2. CALCULAR PRÓXIMA OCURRENCIA DEL SHIFT DESDE "AHORA"
		java.time.ZoneId zone = java.time.ZoneId.of("America/Argentina/Buenos_Aires");
		java.time.LocalDateTime now = java.time.LocalDateTime.now(zone);
		
		java.time.LocalDateTime nextOccurrence = calcularProximaOcurrenciaDesdeAhora(courseSchedule, zone);
		
		// Validar que la próxima ocurrencia esté dentro del rango del curso
		java.time.LocalDate fechaInicioCurso = courseSchedule.getClase().getFechaInicio();
		java.time.LocalDate fechaFinCurso   = courseSchedule.getClase().getFechaFin(); // puede ser null
		
		if (nextOccurrence.toLocalDate().isBefore(fechaInicioCurso)) {
			throw new IllegalStateException("La próxima clase aún no entra en el rango del curso.");
		}
		if (fechaFinCurso != null && nextOccurrence.toLocalDate().isAfter(fechaFinCurso)) {
			throw new IllegalStateException("El curso ya finalizó. No hay próximas clases disponibles.");
		}
		
		// 3. TIEMPO LÍMITE: 1h antes de ESA próxima ocurrencia
		java.time.LocalDateTime tiempoLimiteReserva = nextOccurrence.minusHours(1);
		
		// Chequear que falte al menos 1 hora
		if (!now.isBefore(tiempoLimiteReserva)) {
			throw new IllegalStateException("El tiempo límite para reservar esta clase expiró (1 hora antes del inicio).");
		}


		// 4. VALIDACIONES DE NEGOCIO
		if (courseSchedule.getVacancy() <= 0) {
			throw new IllegalStateException("No quedan cupos disponibles para este curso.");
		}
        
        // Verificar si ya está inscrito activo
		inscripcionRepository.findByUserAndShift(user, courseSchedule)
			.ifPresent(inscripcion -> {
				if ("ACTIVA".equals(inscripcion.getEstado())) {
					throw new IllegalArgumentException("El usuario ya está inscrito y activo en este curso.");
				}
			});
            
        // Verificar si ya tiene una reserva activa para este curso
        reservationRepository.findByIdUserAndIdShift(user.getId(), courseSchedule.getId())
            .ifPresent(reserva -> {
                throw new IllegalArgumentException("El usuario ya tiene una reserva activa para este curso.");
            });
		
		// 5. CREACIÓN DE LA RESERVA con expiración 1 hora antes de la primera clase
		Reservation nuevaReservation = Reservation.builder()
				.idUser(user.getId())
				.idShift(courseSchedule.getId())
				.expiryDate(tiempoLimiteReserva) // Expira 1 hora antes de la primera clase
				.build();

		
		reservationRepository.save(nuevaReservation);

		// 6. ACTUALIZACIÓN DE VACANTES
		courseSchedule.setVacancy(courseSchedule.getVacancy() - 1);
		shiftRepository.save(courseSchedule);
        
        System.out.println("[ReserveService.reserveClass] Reserva creada exitosamente. Expira el: " + tiempoLimiteReserva);
	}
    
    /**
     * Calcula la fecha y hora límite para reservas (1 hora antes de la primera clase)
     */
    private LocalDateTime calcularTiempoLimiteReserva(Shift shift) {
        LocalDate fechaInicioCurso = shift.getClase().getFechaInicio();
        int diaClase = shift.getDiaEnQueSeDicta(); // 1=Lunes, 7=Domingo
        String horaInicio = shift.getHoraInicio(); // formato "HH:mm"
        
        // Encontrar el primer día de clase
        LocalDate primerDiaClase = encontrarPrimerDiaClase(fechaInicioCurso, diaClase);
        
        // Combinar fecha con hora de inicio
        LocalTime horaInicioTime = LocalTime.parse(horaInicio);
        LocalDateTime inicioFirstClass = primerDiaClase.atTime(horaInicioTime);
        
        // Restar 1 hora para el límite de reserva
        return inicioFirstClass.minusHours(1);
    }
    
    /**
     * Encuentra el primer día de clase a partir de la fecha de inicio del curso
     */
    private LocalDate encontrarPrimerDiaClase(LocalDate fechaInicio, int diaClase) {
        DayOfWeek targetDay = DayOfWeek.of(diaClase);
        LocalDate actual = fechaInicio;
        
        // Si la fecha de inicio coincide con el día de clase, esa es la primera clase
        if (actual.getDayOfWeek() == targetDay) {
            return actual;
        }
        
        // Buscar el próximo día que coincida con el día de clase
        while (actual.getDayOfWeek() != targetDay) {
            actual = actual.plusDays(1);
        }
        
        return actual;
    }
    
    /**
     * Tarea programada que se ejecuta cada hora para limpiar reservas expiradas
     */
    @Scheduled(fixedRate = 3600000) // Cada 1 hora
    @Transactional
    public void cleanupExpiredReservations() {
        try {
            System.out.println("[ReserveService.cleanupExpiredReservations] Iniciando limpieza de reservas expiradas...");
            
            LocalDateTime now = LocalDateTime.now();
            List<Reservation> expiredReservations = reservationRepository.findByExpiryDateBefore(now);
            
            for (Reservation reservation : expiredReservations) {
                // Restaurar vacante
                Shift shift = shiftRepository.findById(reservation.getIdShift()).orElse(null);
                if (shift != null) {
                    shift.setVacancy(shift.getVacancy() + 1);
                    shiftRepository.save(shift);
                    System.out.println("[ReserveService.cleanupExpiredReservations] Vacante restaurada para shift ID: " + shift.getId());
                }
                
                // Eliminar reserva
                reservationRepository.delete(reservation);
                System.out.println("[ReserveService.cleanupExpiredReservations] Reserva expirada eliminada: " + reservation.getId());
            }
            
            if (expiredReservations.size() > 0) {
                System.out.println("[ReserveService.cleanupExpiredReservations] Se limpiaron " + expiredReservations.size() + " reservas expiradas");
            }
            
        } catch (Exception e) {
            System.err.println("[ReserveService.cleanupExpiredReservations] Error durante limpieza: " + e.getMessage());
        }
    }
    /**
     * Convierte un número de día (1-7) a su nombre correspondiente
     */
    private String obtenerNombreDia(int diaNumero) {
        String[] diasSemana = {"", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        return (diaNumero >= 1 && diaNumero <= 7) ? diasSemana[diaNumero] : "Día " + diaNumero;
    }
    /**
     * Envía email de confirmación de cancelación de reserva
     */
    private void enviarEmailCancelacionReserva(User user, Course clase, Shift shift) {
        try {
            String subject = "Reserva cancelada - " + clase.getName();
            String diaClase = obtenerNombreDia(shift.getDiaEnQueSeDicta());
            String sedeInfo = (shift.getSede() != null) 
                ? shift.getSede().getName() + " (" + shift.getSede().getAddress() + ")"
                : "Por confirmar";

            String body = String.format(
                "Hola %s,\n\n" +
                "Tu reserva ha sido cancelada exitosamente.\n\n" +
                "--------------------------------------------------\n" +
                "Curso: %s\n" +
                "Día: %s a las %s hs\n" +
                "Sede: %s\n" +
                "Fecha de cancelación: %s\n" +
                "--------------------------------------------------\n\n" +
                "Tu cupo ha sido liberado y estará disponible para otros estudiantes.\n\n" +
                "Si cambias de opinión, puedes volver a reservar este curso (sujeto a disponibilidad).\n\n" +
                "Saludos,\n" +
                "El equipo de RitmoFit",
                user.getUsername(),
                clase.getName(),
                diaClase,
                shift.getHoraInicio(),
                sedeInfo,
                LocalDateTime.now()
            );
            
            emailService.sendEmail(user.getEmail(), subject, body);
            System.out.println("[ReserveService.enviarEmailCancelacionReserva] Email de cancelación enviado a: " + user.getEmail());
            
        } catch (Exception e) {
            System.err.println("[ReserveService.enviarEmailCancelacionReserva] Error enviando email: " + e.getMessage());
            throw e;
        }
    }
	
		/**
	* Calcula la próxima ocurrencia del shift (día/horario) a partir de "ahora".
	* Si hoy es el día y la hora ya pasó, salta a la semana siguiente.
	*/
	private LocalDateTime calcularProximaOcurrenciaDesdeAhora(Shift shift, java.time.ZoneId zone) {
		LocalDateTime now = LocalDateTime.now(zone);
	
		// 1..7 (1=Lunes .. 7=Domingo) según tu modelo
		DayOfWeek targetDow = DayOfWeek.of(shift.getDiaEnQueSeDicta());
	
		// horaInicio formateada "HH:mm"
		LocalTime startTime = LocalTime.parse(shift.getHoraInicio());
	
		LocalDate today = now.toLocalDate();
		LocalDate next = nextOrSame(today, targetDow);
		LocalDateTime startAt = LocalDateTime.of(next, startTime);
	
		// si hoy es el día pero la hora ya pasó -> próxima semana
		if (!startAt.isAfter(now)) {
			startAt = startAt.plusWeeks(1);
		}
		return startAt;
	}
	
	/** nextOrSame sin TemporalAdjusters (para Java 8/11 sin imports extra) */
	private LocalDate nextOrSame(LocalDate date, DayOfWeek target) {
		DayOfWeek d = date.getDayOfWeek();
		int diff = target.getValue() - d.getValue();
		if (diff < 0) diff += 7;
		return date.plusDays(diff);
	}


}
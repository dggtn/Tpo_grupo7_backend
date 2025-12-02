package com.example.g7_back_mobile.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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
    
    // ✅ NUEVO: Autowired del servicio de eventos
    @Autowired
    private UserEventService eventService;

    @Transactional
    public InscripcionExitosaDTO enrollWithReservation(ReservationDTO reservationDTO) {
        System.out.println("[InscriptionService.enrollWithReservation] Procesando inscripción con reserva para usuario: " + reservationDTO.getIdUser());
        
        // 1. BUSCAR LA RESERVA ESPECÍFICA
        Optional<Reservation> reservationOpt = reservationRepository.findByIdUserAndIdShift(
            reservationDTO.getIdUser(), reservationDTO.getIdShift());
        
        if (reservationOpt.isEmpty()) {
            throw new UserException("No se encontró una reserva activa para este usuario y turno. Puede que haya expirado o no exista.");
        }
        
        Reservation reservation = reservationOpt.get();

        // 2. VERIFICAR QUE LA RESERVA NO HAYA EXPIRADO
        if (reservation.getExpiryDate().isBefore(LocalDateTime.now())) {
            System.out.println("[InscriptionService.enrollWithReservation] Reserva expirada, eliminando...");
            reservationRepository.delete(reservation);
            
            // Restaurar la vacante del turno
            Shift shift = shiftRepository.findById(reservationDTO.getIdShift())
                .orElseThrow(() -> new IllegalArgumentException("Cronograma no encontrado con ID: " + reservationDTO.getIdShift()));
            shift.setVacancy(shift.getVacancy() + 1);
            shiftRepository.save(shift);
            
            throw new UserException("El tiempo de reserva ha expirado.");
        }

        // 3. ELIMINAR LA RESERVA Y REESTABLECER LA VACANTE TEMPORALMENTE
        System.out.println("[InscriptionService.enrollWithReservation] Eliminando reserva y procesando inscripción...");
        Shift courseSchedule = shiftRepository.findById(reservationDTO.getIdShift())
            .orElseThrow(() -> new IllegalArgumentException("Cronograma no encontrado con ID: " + reservationDTO.getIdShift()));

        reservationRepository.delete(reservation);
        courseSchedule.setVacancy(courseSchedule.getVacancy() + 1); // Restauramos la vacante temporalmente
        shiftRepository.save(courseSchedule);
        
        // 4. REALIZAR LA INSCRIPCIÓN (que volverá a decrementar la vacante)
        InscripcionExitosaDTO inscripcionExitosaDTO = enrollUser(reservationDTO);
        
        System.out.println("[InscriptionService.enrollWithReservation] Inscripción con reserva completada exitosamente");
        return inscripcionExitosaDTO;
    }

    @Transactional
    public InscripcionExitosaDTO enrollUser(ReservationDTO reservationDTO) {
        System.out.println("[InscriptionService.enrollUser] Iniciando inscripción para usuario: " + reservationDTO.getIdUser());

        // 1. VALIDACIONES BÁSICAS
        if (reservationDTO.getIdUser() == null) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio.");
        }
        if (reservationDTO.getIdShift() == null) {
            throw new IllegalArgumentException("El ID del turno es obligatorio.");
        }
        if (reservationDTO.getMetodoDePago() == null) {
            throw new IllegalArgumentException("El método de pago es obligatorio.");
        }

        // 2. BÚSQUEDA DE ENTIDADES
        User user = userRepository.findById(reservationDTO.getIdUser())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + reservationDTO.getIdUser()));

        Shift courseSchedule = shiftRepository.findById(reservationDTO.getIdShift())
                .orElseThrow(() -> new IllegalArgumentException("Cronograma no encontrado con ID: " + reservationDTO.getIdShift()));
        
        Course clase = courseSchedule.getClase();

        // 3. VERIFICAR QUE EL CURSO NO HAYA COMENZADO (para inscripción directa)
        if (clase.getFechaInicio().isBefore(java.time.LocalDate.now().plusDays(1))) {
            // Si el curso comienza hoy o ya comenzó, solo permitir si hay reserva
            Optional<Reservation> reservaExistente = reservationRepository.findByIdUserAndIdShift(
                reservationDTO.getIdUser(), reservationDTO.getIdShift());
            
            if (reservaExistente.isEmpty()) {
                throw new IllegalStateException("No se puede inscribir directamente a un curso que ya ha comenzado o comienza hoy. Debe tener una reserva previa.");
            }
        }

        // 4. VERIFICAR SI EL USUARIO TIENE UNA RESERVA PARA ESTE TURNO Y ELIMINARLA
        Optional<Reservation> existingReservation = reservationRepository.findByIdUserAndIdShift(
            reservationDTO.getIdUser(), reservationDTO.getIdShift());
        
        if (existingReservation.isPresent()) {
            System.out.println("[InscriptionService.enrollUser] Usuario tiene reserva existente, eliminando automáticamente...");
            Reservation reservation = existingReservation.get();
            reservationRepository.delete(reservation);
            
            // Restaurar la vacante que estaba ocupada por la reserva
            courseSchedule.setVacancy(courseSchedule.getVacancy() + 1);
            shiftRepository.save(courseSchedule);
            
            System.out.println("[InscriptionService.enrollUser] Reserva eliminada automáticamente durante inscripción directa");
        }

        // 5. VALIDACIONES DE NEGOCIO
        if (courseSchedule.getVacancy() <= 0) {
            throw new IllegalStateException("No quedan cupos disponibles para este curso.");
        }
        
        // Verificar si ya está inscrito y activo
        Optional<Inscription> existingInscription = inscripcionRepository.findByUserAndShift(user, courseSchedule);
        if (existingInscription.isPresent()) {
            if ("ACTIVA".equals(existingInscription.get().getEstado())) {
                throw new IllegalArgumentException("El usuario ya está inscrito y activo en este curso.");
            }
        }
        
        // 6. VALIDACIÓN DEL MÉTODO DE PAGO
        if (reservationDTO.getMetodoDePago() != MetodoDePago.CREDIT_CARD && 
            reservationDTO.getMetodoDePago() != MetodoDePago.DEBIT_CARD) {
            throw new IllegalStateException("Método de pago no válido. Use CREDIT_CARD o DEBIT_CARD.");
        }
        
        // Aquí iría la lógica de procesamiento de pago
        double precioCurso = clase.getPrice();
        System.out.println("[InscriptionService.enrollUser] Procesando pago por $" + precioCurso + " con método: " + reservationDTO.getMetodoDePago());
        
        // 7. CREACIÓN DE LA INSCRIPCIÓN
        Inscription nuevaInscripcion = Inscription.builder()
                .user(user)
                .shift(courseSchedule)
                .fechaInscripcion(LocalDateTime.now())
                .estado("ACTIVA")
                .build();
        Inscription savedInscripcion = inscripcionRepository.save(nuevaInscripcion);

        // 8. ACTUALIZACIÓN DE VACANTES
        courseSchedule.setVacancy(courseSchedule.getVacancy() - 1);
        shiftRepository.save(courseSchedule);
        
        System.out.println("[InscriptionService.enrollUser] Inscripción creada con ID: " + savedInscripcion.getId());
        
        // GENERAR EVENTO DE INSCRIPCIÓN
        try {
            eventService.createEnrollmentEvent(user.getId(), clase, courseSchedule);
            System.out.println("[InscriptionService.enrollUser] ✅ Evento de inscripción generado");
        } catch (Exception e) {
            System.err.println("[InscriptionService.enrollUser] ❌ Error generando evento de inscripción: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            generarRecordatoriosDeClases(user.getId(), clase, courseSchedule);
            System.out.println("[InscriptionService.enrollUser] ✅ Recordatorios de clases generados");
        } catch (Exception e) {
            System.err.println("[InscriptionService.enrollUser] ❌ Error generando recordatorios: " + e.getMessage());
            e.printStackTrace();
        }
        // 9. ENVÍO DE EMAIL DE CONFIRMACIÓN
        try {
            enviarEmailConfirmacion(user, clase, courseSchedule);
        } catch (Exception e) {
            System.err.println("[InscriptionService.enrollUser] Error al enviar email de confirmación: " + e.getMessage());
            // No fallar la inscripción por un error de email
        }

        // 10. DEVOLVER RESPUESTA
        return new InscripcionExitosaDTO(
                savedInscripcion.getId(),
                clase.getName(),
                user.getEmail(),
                savedInscripcion.getFechaInscripcion(),
                savedInscripcion.getEstado()
        );
    }

    /**
     * ✅ NUEVO MÉTODO: Genera recordatorios para todas las clases futuras del curso
     */
    private void generarRecordatoriosDeClases(Long userId, Course curso, Shift shift) {
        try {
            LocalDate hoy = LocalDate.now();
            LocalDate fechaInicio = curso.getFechaInicio();
            LocalDate fechaFin = curso.getFechaFin();
            
            // Si el curso ya empezó, empezar desde hoy
            LocalDate fechaDesde = fechaInicio.isBefore(hoy) ? hoy : fechaInicio;
            
            int diaClase = shift.getDiaEnQueSeDicta(); // 1=Lunes, 7=Domingo
            LocalTime horaInicio = LocalTime.parse(shift.getHoraInicio());
            
            System.out.println("[InscriptionService.generarRecordatoriosDeClases] Generando para userId=" 
                + userId + ", curso=" + curso.getName() + ", día=" + diaClase);
            
            int recordatoriosGenerados = 0;
            LocalDate fecha = fechaDesde;
            
            // Iterar por todas las fechas del curso
            while (!fecha.isAfter(fechaFin)) {
                // Si coincide con el día de la clase
                if (fecha.getDayOfWeek().getValue() == diaClase) {
                    LocalDateTime fechaHoraClase = LocalDateTime.of(fecha, horaInicio);
                    
                    // Solo generar recordatorios para clases futuras
                    if (fechaHoraClase.isAfter(LocalDateTime.now())) {
                        eventService.createClassReminderEvent(userId, curso, shift, fechaHoraClase);
                        recordatoriosGenerados++;
                        System.out.println("[InscriptionService] 📅 Recordatorio para: " + fechaHoraClase);
                    }
                }
                fecha = fecha.plusDays(1);
            }
            
            System.out.println("[InscriptionService.generarRecordatoriosDeClases] ✅ " 
                + recordatoriosGenerados + " recordatorios generados");
            
        } catch (Exception e) {
            System.err.println("[InscriptionService.generarRecordatoriosDeClases] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     *  Cancelar inscripción y generar evento
     */
    @Transactional
    public void cancelInscription(Long inscriptionId, Long userId) {
        System.out.println("[InscriptionService.cancelInscription] Cancelando inscripción ID: " + inscriptionId + " para usuario: " + userId);
        
        // Buscar la inscripción
        Inscription inscription = inscripcionRepository.findById(inscriptionId)
            .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada con ID: " + inscriptionId));

        // Verificar que el usuario sea el dueño de la inscripción
        if (!inscription.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("No autorizado para cancelar esta inscripción. El usuario no coincide.");
        }

        // Verificar que la inscripción esté activa
        if (!"ACTIVA".equals(inscription.getEstado())) {
            throw new IllegalStateException("La inscripción no está activa. Estado actual: " + inscription.getEstado());
        }

        Shift shift = inscription.getShift();
        Course course = shift.getClase();

        // Cambiar estado a CANCELADA
        inscription.setEstado("CANCELADA");
        inscripcionRepository.save(inscription);

        // Liberar vacante
        shift.setVacancy(shift.getVacancy() + 1);
        shiftRepository.save(shift);

        System.out.println("[InscriptionService.cancelInscription] Inscripción cancelada exitosamente. Vacante liberada.");

        // ✅ NUEVO: GENERAR EVENTO DE CANCELACIÓN
        try {
            eventService.createCancellationEvent(userId, course, shift, "Inscripción cancelada por el usuario");
            System.out.println("[InscriptionService.cancelInscription] Evento de cancelación generado correctamente");
        } catch (Exception e) {
            System.err.println("[InscriptionService.cancelInscription] Error generando evento de cancelación: " + e.getMessage());
            // No fallar la cancelación por un error en el evento
        }

        System.out.println("[InscriptionService.cancelInscription] Inscripción cancelada y evento generado para userId=" + userId + ", shiftId=" + shift.getId());
    }
    
    private void enviarEmailConfirmacion(User user, Course clase, Shift courseSchedule) {
        try {
            String subject = "¡Confirmación de tu inscripción al curso: " + clase.getName() + "!";
            String precioFormateado = String.format("$%.2f", clase.getPrice());

            String diaClase = obtenerNombreDia(courseSchedule.getDiaEnQueSeDicta());
            String sedeInfo = (courseSchedule.getSede() != null) 
                ? courseSchedule.getSede().getName() + " (" + courseSchedule.getSede().getAddress() + ")"
                : "Por confirmar";
            String profesorInfo = (courseSchedule.getTeacher() != null) 
                ? courseSchedule.getTeacher().getName()
                : "Por asignar";

            String body = String.format(
                "Hola %s,\n\n" +
                "¡Te has inscrito exitosamente! Aquí están los detalles de tu curso:\n\n" +
                "--------------------------------------------------\n" +
                "Curso: %s\n" +
                "Instructor: %s\n" +
                "Duración: %d semanas\n" +
                "Costo: %s\n" +
                "Fechas: %s al %s\n" +
                "--------------------------------------------------\n\n" +
                "Detalles del Horario:\n" +
                "Sede: %s\n" +
                "Día: %s\n" +
                "Horario: de %s a %s hs.\n\n" +
                "¡Te esperamos!\n\n" +
                "Saludos,\n" +
                "El equipo de RitmoFit",
                user.getUsername(),
                clase.getName(),
                profesorInfo,
                clase.getLength(),
                precioFormateado,
                clase.getFechaInicio(),
                clase.getFechaFin(),
                sedeInfo,
                diaClase,
                courseSchedule.getHoraInicio(),
                courseSchedule.getHoraFin()
            );
            
            emailService.sendEmail(user.getEmail(), subject, body);
            System.out.println("[InscriptionService.enviarEmailConfirmacion] Email enviado a: " + user.getEmail());
            
        } catch (Exception e) {
            System.err.println("[InscriptionService.enviarEmailConfirmacion] Error enviando email: " + e.getMessage());
            throw e;
        }
    }
    
    private String obtenerNombreDia(int diaNumero) {
        String[] diasSemana = {"", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        return (diaNumero >= 1 && diaNumero <= 7) ? diasSemana[diaNumero] : "Día " + diaNumero;
    }
    
    public List<Inscription> getUserInscriptions(Long studentId) throws Exception {
        try {
            if (studentId == null) {
                throw new IllegalArgumentException("El ID del estudiante es obligatorio.");
            }
            
            return inscripcionRepository.findByUserId(studentId);
            
        } catch (Exception error) {
            System.err.println("[InscriptionService.getUserInscriptions] Error: " + error.getMessage());
            throw new Exception("[InscriptionService.getUserInscriptions] -> " + error.getMessage());
        }
    }
}
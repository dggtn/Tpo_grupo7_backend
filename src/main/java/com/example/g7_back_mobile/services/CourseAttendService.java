package com.example.g7_back_mobile.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.g7_back_mobile.controllers.dtos.AsistenciaDTO;
import com.example.g7_back_mobile.controllers.dtos.AsistenciaResultadoDTO;
import com.example.g7_back_mobile.repositories.CourseAttendRepository;
import com.example.g7_back_mobile.repositories.InscriptionRepository;
import com.example.g7_back_mobile.repositories.ReservationRepository;
import com.example.g7_back_mobile.repositories.ShiftRepository;
import com.example.g7_back_mobile.repositories.UserRepository;
import com.example.g7_back_mobile.repositories.entities.CourseAttend;
import com.example.g7_back_mobile.repositories.entities.Course;
import com.example.g7_back_mobile.repositories.entities.Inscription;
import com.example.g7_back_mobile.repositories.entities.Reservation;
import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.repositories.entities.User;

import jakarta.transaction.Transactional;

@Service
public class CourseAttendService {

    @Autowired
    private InscriptionRepository inscripcionRepository;

    @Autowired
    private CourseAttendRepository courseAttendRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ShiftRepository shiftRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private EmailService emailService;

    @Transactional
    public CourseAttend registrarAsistencia(AsistenciaDTO dto) {
        
        // 1. VALIDACIONES BÁSICAS
        if (dto.getIdUser() == null) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio.");
        }
        if (dto.getIdCronograma() == null) {
            throw new IllegalArgumentException("El ID del cronograma es obligatorio.");
        }
        
        // 2. VERIFICAR QUE EL USUARIO EXISTE
        User user = userRepository.findById(dto.getIdUser())
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + dto.getIdUser()));
        
        // 3. VERIFICAR QUE EL CRONOGRAMA/SHIFT EXISTE
        Shift shift = shiftRepository.findById(dto.getIdCronograma())
            .orElseThrow(() -> new IllegalArgumentException("Cronograma no encontrado con ID: " + dto.getIdCronograma()));
        
        LocalDate hoy = LocalDate.now();
        
        // 4. VERIFICAR QUE HOY ES EL DÍA CORRECTO PARA LA CLASE
        DayOfWeek diaHoy = hoy.getDayOfWeek();
        int diaHoyNumero = diaHoy.getValue(); // Lunes=1, Domingo=7
        
        if (diaHoyNumero != shift.getDiaEnQueSeDicta()) {
            String[] diasSemana = {"", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
            String diaEsperado = shift.getDiaEnQueSeDicta() >= 1 && shift.getDiaEnQueSeDicta() <= 7 
                ? diasSemana[shift.getDiaEnQueSeDicta()] 
                : "Día " + shift.getDiaEnQueSeDicta();
            String diaActual = diasSemana[diaHoyNumero];
            throw new IllegalStateException("Hoy es " + diaActual + ", pero la clase es el " + diaEsperado + ".");
        }
        
        // 5. VERIFICAR QUE LA FECHA ESTÁ DENTRO DEL RANGO DEL CURSO
        LocalDate fechaInicio = shift.getClase().getFechaInicio();
        LocalDate fechaFin = shift.getClase().getFechaFin();
        
        if (hoy.isBefore(fechaInicio)) {
            throw new IllegalStateException("El curso aún no ha comenzado. Fecha de inicio: " + fechaInicio);
        }
        
        if (hoy.isAfter(fechaFin)) {
            throw new IllegalStateException("El curso ya ha finalizado. Fecha de fin: " + fechaFin);
        }

        // 6. VERIFICAR SI ES LA PRIMERA CLASE DEL CURSO
        boolean esPrimeraClase = esLaPrimeraClaseDelCurso(shift, hoy);
        System.out.println("[CourseAttendService.registrarAsistencia] ¿Es primera clase? " + esPrimeraClase);

        // 7. BUSCAR INSCRIPCIÓN ACTIVA O MANEJAR RESERVA
        Optional<Inscription> inscripcionOpt = inscripcionRepository
                .findByUserIdAndShiftIdAndEstado(dto.getIdUser(), dto.getIdCronograma(), "ACTIVA");
        
        Inscription inscripcion;
        boolean inscripcionCreada = false;
        
        if (inscripcionOpt.isEmpty()) {
            // No hay inscripción activa, verificar si hay reserva y es primera clase
            if (esPrimeraClase) {
                inscripcion = manejarInscripcionAutomaticaConReserva(user, shift);
                inscripcionCreada = true;
                System.out.println("[CourseAttendService.registrarAsistencia] Inscripción automática realizada para primera clase");
            } else {
                throw new IllegalArgumentException("No se encontró una inscripción activa para este usuario y cronograma.");
            }
        } else {
            inscripcion = inscripcionOpt.get();
            System.out.println("[CourseAttendService.registrarAsistencia] Usando inscripción existente ID: " + inscripcion.getId());
        }

        // 8. VERIFICAR QUE NO HAYA REGISTRADO ASISTENCIA HOY
        boolean yaAsistioHoy = courseAttendRepository
                .existsByInscripcionAndFechaAsistencia(inscripcion, hoy);

        if (yaAsistioHoy) {
            throw new IllegalStateException("Ya registró su asistencia para el día de hoy (" + hoy + ").");
        }

        // 9. CREAR EL NUEVO REGISTRO DE ASISTENCIA
        CourseAttend nuevaAsistencia = CourseAttend.builder()
                .inscripcion(inscripcion)
                .fechaAsistencia(hoy)
                .presente(true)
                .build();

        CourseAttend asistenciaGuardada = courseAttendRepository.save(nuevaAsistencia);
        
        // 10. ENVIAR EMAIL DE CONFIRMACIÓN SI SE CREÓ INSCRIPCIÓN AUTOMÁTICA
        if (inscripcionCreada) {
            try {
                enviarEmailInscripcionAutomatica(user, shift.getClase(), shift);
            } catch (Exception e) {
                System.err.println("[CourseAttendService.registrarAsistencia] Error enviando email: " + e.getMessage());
            }
        }
        
        System.out.println("[CourseAttendService.registrarAsistencia] Asistencia registrada exitosamente para usuario " 
            + user.getUsername() + " en fecha " + hoy);
        
        return asistenciaGuardada;
    }

    /**
     * Maneja la inscripción automática cuando hay una reserva y es la primera clase
     */
    @Transactional
    private Inscription manejarInscripcionAutomaticaConReserva(User user, Shift shift) {
        
        // Buscar reserva activa
        Optional<Reservation> reservaOpt = reservationRepository
                .findByIdUserAndIdShift(user.getId(), shift.getId());
        
        if (reservaOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró una reserva activa ni inscripción para este usuario y cronograma.");
        }
        
        Reservation reserva = reservaOpt.get();
        
        // Verificar que la reserva no haya expirado (aunque debería estar vigente para la primera clase)
        if (reserva.getExpiryDate().isBefore(LocalDateTime.now())) {
            // Limpiar reserva expirada
            reservationRepository.delete(reserva);
            shift.setVacancy(shift.getVacancy() + 1);
            shiftRepository.save(shift);
            throw new IllegalStateException("La reserva ha expirado y no se puede procesar la inscripción automática.");
        }
        
        // Crear inscripción automática
        Inscription nuevaInscripcion = Inscription.builder()
                .user(user)
                .shift(shift)
                .fechaInscripcion(LocalDateTime.now())
                .estado("ACTIVA")
                .build();
        
        Inscription inscripcionGuardada = inscripcionRepository.save(nuevaInscripcion);
        
        // Eliminar la reserva (ya se convirtió en inscripción)
        reservationRepository.delete(reserva);
        
        // La vacante ya está ocupada por la reserva, no necesitamos modificarla
        
        System.out.println("[CourseAttendService.manejarInscripcionAutomaticaConReserva] Inscripción automática creada con ID: " 
            + inscripcionGuardada.getId());
        
        return inscripcionGuardada;
    }
    
    /**
     * Verifica si hoy es la primera clase del curso
     */
    private boolean esLaPrimeraClaseDelCurso(Shift shift, LocalDate hoy) {
        LocalDate fechaInicio = shift.getClase().getFechaInicio();
        int diaClase = shift.getDiaEnQueSeDicta();
        
        // Encontrar el primer día de clase
        LocalDate primerDiaClase = encontrarPrimerDiaClase(fechaInicio, diaClase);
        
        return hoy.equals(primerDiaClase);
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
     * Envía email de confirmación para inscripción automática
     */
    private void enviarEmailInscripcionAutomatica(User user, Course clase, Shift shift) {
        try {
            String subject = "¡Inscripción automática confirmada - " + clase.getName() + "!";
            String precioFormateado = String.format("$%.2f", clase.getPrice());
            
            String diaClase = obtenerNombreDia(shift.getDiaEnQueSeDicta());
            String sedeInfo = (shift.getSede() != null) 
                ? shift.getSede().getName() + " (" + shift.getSede().getAddress() + ")"
                : "Por confirmar";
            String profesorInfo = (shift.getTeacher() != null) 
                ? shift.getTeacher().getName()
                : "Por asignar";

            String body = String.format(
                "Hola %s,\n\n" +
                "¡Tu reserva se ha convertido automáticamente en inscripción al registrar tu asistencia a la primera clase!\n\n" +
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
                "Tu asistencia de hoy ya ha sido registrada.\n\n" +
                "¡Bienvenido al curso!\n\n" +
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
                shift.getHoraInicio(),
                shift.getHoraFin()
            );
            
            emailService.sendEmail(user.getEmail(), subject, body);
            System.out.println("[CourseAttendService.enviarEmailInscripcionAutomatica] Email enviado a: " + user.getEmail());
            
        } catch (Exception e) {
            System.err.println("[CourseAttendService.enviarEmailInscripcionAutomatica] Error enviando email: " + e.getMessage());
            throw e;
        }
    }
    
    private String obtenerNombreDia(int diaNumero) {
        String[] diasSemana = {"", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        return (diaNumero >= 1 && diaNumero <= 7) ? diasSemana[diaNumero] : "Día " + diaNumero;
    }

    public AsistenciaResultadoDTO verificarAsistencia(Long idInscripcion) {
        
        // 1. VALIDACIÓN BÁSICA
        if (idInscripcion == null) {
            throw new IllegalArgumentException("El ID de inscripción es obligatorio.");
        }
        
        // 2. BUSCAR LA INSCRIPCIÓN
        Inscription inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada con ID: " + idInscripcion));

        // 3. CONTAR LAS CLASES A LAS QUE ASISTIÓ
        long clasesAsistidas = courseAttendRepository.countByInscripcion(inscripcion);

        // 4. CALCULAR EL NÚMERO TOTAL DE CLASES DEL CURSO
        int totalClases = contarClasesTotales(inscripcion.getShift());

        if (totalClases == 0) {
            return new AsistenciaResultadoDTO(0, 0, "El curso no tiene clases programadas o las fechas son inválidas.");
        }

        // 5. GENERAR MENSAJE INFORMATIVO
        String mensaje = String.format("Asistencia: %d de %d clases (%.1f%%)", 
            clasesAsistidas, totalClases, (clasesAsistidas * 100.0) / totalClases);

        return new AsistenciaResultadoDTO(totalClases, clasesAsistidas, mensaje);
    }

    // Método auxiliar para contar el total de clases
    private int contarClasesTotales(Shift cronograma) {
        try {
            LocalDate fechaInicio = cronograma.getClase().getFechaInicio();
            LocalDate fechaFin = cronograma.getClase().getFechaFin();
            int diaClase = cronograma.getDiaEnQueSeDicta();
            
            // Validar que el día esté en rango válido (1-7)
            if (diaClase < 1 || diaClase > 7) {
                System.err.println("[CourseAttendService.contarClasesTotales] Día inválido: " + diaClase);
                return 0;
            }
            
            DayOfWeek diaDeClase = DayOfWeek.of(diaClase);
            int totalClases = 0;
            LocalDate diaActual = fechaInicio;

            // Buscar el primer día que coincida con el día de la clase
            while (!diaActual.isAfter(fechaFin)) {
                if (diaActual.getDayOfWeek() == diaDeClase) {
                    totalClases++;
                }
                diaActual = diaActual.plusDays(1);
            }
            
            System.out.println("[CourseAttendService.contarClasesTotales] Curso: " + cronograma.getClase().getName() 
                + ", Total clases calculadas: " + totalClases + " (desde " + fechaInicio + " hasta " + fechaFin + ", día " + diaDeClase + ")");
            
            return totalClases;
            
        } catch (Exception e) {
            System.err.println("[CourseAttendService.contarClasesTotales] Error calculando total de clases: " + e.getMessage());
            return 0;
        }
    }
}
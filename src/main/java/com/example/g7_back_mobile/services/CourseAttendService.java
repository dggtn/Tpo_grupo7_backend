package com.example.g7_back_mobile.services;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.g7_back_mobile.controllers.dtos.AsistenciaDTO;
import com.example.g7_back_mobile.controllers.dtos.AsistenciaResultadoDTO;
import com.example.g7_back_mobile.repositories.CourseAttendRepository;
import com.example.g7_back_mobile.repositories.InscriptionRepository;
import com.example.g7_back_mobile.repositories.ShiftRepository;
import com.example.g7_back_mobile.repositories.UserRepository;
import com.example.g7_back_mobile.repositories.entities.CourseAttend;
import com.example.g7_back_mobile.repositories.entities.Inscription;
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
        
        // 4. BUSCAR LA INSCRIPCIÓN ACTIVA
        Inscription inscripcion = inscripcionRepository
                .findByUserIdAndShiftIdAndEstado(dto.getIdUser(), dto.getIdCronograma(), "ACTIVA")
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una inscripción activa para este usuario y cronograma."));

        LocalDate hoy = LocalDate.now();
        
        // 5. VERIFICAR QUE HOY ES EL DÍA CORRECTO PARA LA CLASE
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
        
        // 6. VERIFICAR QUE LA FECHA ESTÁ DENTRO DEL RANGO DEL CURSO
        LocalDate fechaInicio = shift.getClase().getFechaInicio();
        LocalDate fechaFin = shift.getClase().getFechaFin();
        
        if (hoy.isBefore(fechaInicio)) {
            throw new IllegalStateException("El curso aún no ha comenzado. Fecha de inicio: " + fechaInicio);
        }
        
        if (hoy.isAfter(fechaFin)) {
            throw new IllegalStateException("El curso ya ha finalizado. Fecha de fin: " + fechaFin);
        }

        // 7. VERIFICAR QUE NO HAYA REGISTRADO ASISTENCIA HOY
        boolean yaAsistioHoy = courseAttendRepository
                .existsByInscripcionAndFechaAsistencia(inscripcion, hoy);

        if (yaAsistioHoy) {
            throw new IllegalStateException("Ya registró su asistencia para el día de hoy (" + hoy + ").");
        }

        // 8. CREAR EL NUEVO REGISTRO DE ASISTENCIA
        CourseAttend nuevaAsistencia = CourseAttend.builder()
                .inscripcion(inscripcion)
                .fechaAsistencia(hoy)
                .presente(true)
                .build();

        CourseAttend asistenciaGuardada = courseAttendRepository.save(nuevaAsistencia);
        
        System.out.println("[CourseAttendService.registrarAsistencia] Asistencia registrada exitosamente para usuario " 
            + user.getUsername() + " en fecha " + hoy);
        
        return asistenciaGuardada;
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

    // Método auxiliar para contar el total de clases - CORREGIDO
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
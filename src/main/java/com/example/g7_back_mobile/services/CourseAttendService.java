package com.example.g7_back_mobile.services;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.g7_back_mobile.controllers.dtos.AsistenciaDTO;
import com.example.g7_back_mobile.controllers.dtos.AsistenciaResultadoDTO;
import com.example.g7_back_mobile.repositories.CourseAttendRepository;
import com.example.g7_back_mobile.repositories.InscriptionRepository;
import com.example.g7_back_mobile.repositories.entities.CourseAttend;
import com.example.g7_back_mobile.repositories.entities.Inscription;
import com.example.g7_back_mobile.repositories.entities.Shift;

import jakarta.transaction.Transactional;

@Service
public class CourseAttendService {

    @Autowired
    private InscriptionRepository inscripcionRepository;

    @Autowired
    private CourseAttendRepository courseAttendRepository;

    @Transactional
	public CourseAttend registrarAsistencia(AsistenciaDTO dto) {
		// 1. Buscamos la inscripción activa del alumno para ese cronograma
		Inscription inscripcion = inscripcionRepository
				.findByUserIdAndShiftIdAndEstado(dto.getIdUser(), dto.getIdCronograma(), "ACTIVA")
				.orElseThrow(() -> new IllegalArgumentException("No se encontró una inscripción activa para este usuario y curso."));

		LocalDate hoy = LocalDate.now();

		// 2. Verificamos que no haya registrado asistencia hoy para esta inscripción
		boolean yaAsistioHoy = courseAttendRepository
				.existsByInscripcionAndFechaAsistencia(inscripcion, hoy);

		if (yaAsistioHoy) {
			throw new IllegalStateException("El usuario ya registró su asistencia para el día de hoy.");
		}

		// 3. Creamos el nuevo registro de asistencia
		CourseAttend nuevaAsistencia = CourseAttend.builder()
				.inscripcion(inscripcion)
				.fechaAsistencia(hoy)
				.presente(true)
				.build();

		return courseAttendRepository.save(nuevaAsistencia);
	}

	public AsistenciaResultadoDTO verificarAsistencia(Long idInscripcion) {
		// 1. Buscamos la inscripción
		Inscription inscripcion = inscripcionRepository.findById(idInscripcion)
				.orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada con ID: " + idInscripcion));

		// 2. Contamos las clases a las que asistió
		long clasesAsistidas = courseAttendRepository.countByInscripcion(inscripcion);

		// 3. Calculamos el número total de clases del curso
		int totalClases = contarClasesTotales(inscripcion.getShift());

		if (totalClases == 0) {
			return new AsistenciaResultadoDTO(0, 0, "El curso no tiene clases programadas.");
		}

        return new AsistenciaResultadoDTO(totalClases, clasesAsistidas, "");
		
	}

	// Método auxiliar para contar el total de clases
	private int contarClasesTotales(Shift cronograma) {
		LocalDate fechaInicio = cronograma.getClase().getFechaInicio();
		LocalDate fechaFin = cronograma.getClase().getFechaFin();
		DayOfWeek diaDeClase = DayOfWeek.of(cronograma.getDiaEnQueSeDicta());

		int totalClases = 0;
		LocalDate diaActual = fechaInicio;

		while (!diaActual.isAfter(fechaFin)) {
			if (diaActual.getDayOfWeek() == diaDeClase) {
				totalClases++;
			}
			// CAMBIO CLAVE: Avanzamos día por día en lugar de semana por semana
			diaActual = diaActual.plusDays(1);
		}
		return totalClases;
	}
    
}

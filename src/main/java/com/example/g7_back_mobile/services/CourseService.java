package com.example.g7_back_mobile.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.g7_back_mobile.controllers.dtos.CourseDTO;
import com.example.g7_back_mobile.repositories.CourseRepository;
import com.example.g7_back_mobile.repositories.entities.Course;
import com.example.g7_back_mobile.services.exceptions.CourseException;

import jakarta.transaction.Transactional;
@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    public List<Course> todosLosCursos() throws CourseException {
			List<Course> cursos = courseRepository.findAll();
			if (cursos.isEmpty()) {
				throw new CourseException("No se encontraron cursos en la base de datos.");
			}
			return cursos;
	}
	

	public Course getCourseById(Long courseId) throws Exception {
		try{
			return courseRepository.findById(courseId).orElseThrow(() -> new CourseException("Curso no encontrado"));
		} catch (CourseException error) {
			throw new CourseException(error.getMessage());
		} catch (Exception error) {
			throw new Exception("[Controlador.getCourseByName] -> " + error.getMessage());
		}
	}

	public CourseDTO getCourseDTOById(Long courseId) throws Exception {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new CourseException("Curso no encontrado con ID: " + courseId));
		return course.toDTO(); // Devolvemos la vista en lugar de la entidad
	}

	public Course createCourse(Course course) throws Exception {

          try {
			
			Course createdCourse = courseRepository.save(course);
            return createdCourse;
          } catch (Exception error) {
            throw new Exception("[Controlador.createCourse] -> " + error.getMessage());
          }
    }

	public void inicializarCursos() throws Exception {
		try {

			Course course1 = new Course(null, "Natación Adultos", null,
					LocalDate.parse("2025-08-08"), // <-- CAMBIO AQUÍ
					LocalDate.parse("2025-11-08"), // <-- CAMBIO AQUÍ
					50,
					50.0,
					"imgA.jpg",
					new ArrayList<>(),
					new ArrayList<>(),
					new ArrayList<>()
			);

			Course cours2 = new Course(null, "Karate", null,
					LocalDate.parse("2025-07-18"), // <-- CAMBIO AQUÍ
					LocalDate.parse("2025-08-18"), // <-- CAMBIO AQUÍ
					50,
					40.0,
					"imgB.jpg",
					new ArrayList<>(),
					new ArrayList<>(),
					new ArrayList<>()
			);

			Course course3 = new Course(null, "Pilates", null,
				    LocalDate.parse("2025-07-11"), // <-- CAMBIO AQUÍ
					LocalDate.parse("2025-08-11"), // <-- CAMBIO AQUÍ
					50,
					42.0,
					"imgC.jpg",
					new ArrayList<>(),
					new ArrayList<>(),
					new ArrayList<>()
			);

			courseRepository.save(course1);
			courseRepository.save(cours2);
			courseRepository.save(course3);

		} catch (CourseException error) {
			throw new CourseException(error.getMessage());
		} catch (Exception error) {
			throw new Exception("[Service.inicializarCursos] -> " + error.getMessage());
		}
	}

	public Course updateCourse(Course course) throws Exception {
          try {
            if (!courseRepository.existsById(course.getId())) 
              throw new CourseException("El curso con id: '" + course.getId() + "' no existe.");
            
            Course updatedCourse = courseRepository.save(course);
            return updatedCourse;
          } catch (CourseException error) {
            throw new CourseException(error.getMessage());
          } catch (Exception error) {
            throw new Exception("[Controlador.updateCourse] -> " + error.getMessage());
          }
    }

	@Transactional
    public void deleteCourse(Long id) throws Exception {
          try {
              courseRepository.deleteById(id);
          } catch (Exception error) {
            throw new Exception("[Controlador.deleteCourse] -> " + error.getMessage());
          }
    }
    
    
    public List<CourseDTO> findCoursesByName(String name) {
		return courseRepository.findByNameContainingIgnoreCase(name)
				.stream()
				.map(Course::toDTO) // Usamos el método que ya tenías para convertir
				.collect(Collectors.toList());
	}
}

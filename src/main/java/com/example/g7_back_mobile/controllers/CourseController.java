package com.example.g7_back_mobile.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.g7_back_mobile.controllers.dtos.CourseDTO;
import com.example.g7_back_mobile.controllers.dtos.ResponseData;
import com.example.g7_back_mobile.repositories.entities.Course;
import com.example.g7_back_mobile.services.CourseService;
import com.example.g7_back_mobile.services.exceptions.CourseException;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;
    

    @GetMapping("/allCourses")
    public ResponseEntity<ResponseData<?>> obtenerTodosLosCursos() {
        try {
            List<Course> cursos = courseService.todosLosCursos();
            return ResponseEntity.ok(ResponseData.success(cursos));
        } catch (CourseException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseData.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("Error inesperado: " + e.getMessage()));
        }
    }
   
    //Se puede crear un curso nuevo con un body por postman:
    @PostMapping("")
    public ResponseEntity<ResponseData<?>> createCourse(@RequestBody CourseDTO courseDTO) {
        try {
        courseDTO.setId(null);

        Course course = courseDTO.toEntity();

        Course createdCourse = courseService.createCourse(course);

        CourseDTO createdCourseDTO = createdCourse.toDTO();

        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseData.success(createdCourseDTO));

        } catch (Exception error) {
        System.out.printf("[CourseController.createCourse] -> %s", error.getMessage() );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("No se pudo crear el curso"));
        }
    }

    //cursos ya creados en el service:
    @PostMapping("/initializeCourses")
    public ResponseEntity<ResponseData<String>> initializeCoursesDB() {
        try {

            courseService.inicializarCursos();
            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success("Base inicializada correctamente!"));

        } catch (CourseException error) {
            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.error(error.getMessage()));
        } catch (Exception error) {
            System.out.printf("[ApiCourse.initializeCoursesDB] -> %s", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ResponseData.error("No se pudo inicializar la DB"));
        }
    }

    @PutMapping("")
    public ResponseEntity<ResponseData<?>> updateCourse(@RequestBody CourseDTO courseDTO) {
        try {
        Course course = courseDTO.toEntity();
        Course updatedCourse = courseService.updateCourse(course);
        CourseDTO updatedCourseDTO = updatedCourse.toDTO();
        return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(updatedCourseDTO));

        }catch (CourseException error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseData.error(error.getMessage()));

        } catch (Exception error) {
        System.out.printf("[CourseController.updateCourse] -> %s", error.getMessage() );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("No se pudo actualizar el curso"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<?>> deleteCourse(@PathVariable("id") Long id) {
        try {
        courseService.deleteCourse(id);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(null));

        } catch (Exception error) {
        System.out.printf("[CourseController.deleteCourse] -> %s", error.getMessage() );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("No se pudo eliminar el producto"));
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<ResponseData<List<CourseDTO>>> searchCoursesByName(@RequestParam String nombre) {
        List<CourseDTO> cursos = courseService.findCoursesByName(nombre);
        return ResponseEntity.ok(ResponseData.success(cursos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<CourseDTO>> getCourseDetails(@PathVariable Long id) {
        try {
            CourseDTO courseView = courseService.getCourseDTOById(id);
            return ResponseEntity.ok(ResponseData.success(courseView));
        } catch (Exception e) {
            // Si no se encuentra el curso, devolvemos un 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }
    
}

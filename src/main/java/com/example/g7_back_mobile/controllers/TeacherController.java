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
import org.springframework.web.bind.annotation.RestController;

import com.example.g7_back_mobile.controllers.dtos.ResponseData;
import com.example.g7_back_mobile.controllers.dtos.TeacherDTO;
import com.example.g7_back_mobile.repositories.entities.Teacher;
import com.example.g7_back_mobile.services.TeacherService;
import com.example.g7_back_mobile.services.exceptions.TeacherException;

@RestController
@RequestMapping("/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/allTeacherss")
    public ResponseEntity<?> obtenerTodosLosProfes() {
        try {
            List<Teacher> profes = teacherService.todosLosProfes();
            return ResponseEntity.ok(profes);
        } catch (TeacherException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado: " + e.getMessage());
        }
    }

    @PostMapping("/initializeTeachers")
    public ResponseEntity<ResponseData<String>> initializeTeachersDB() {
        try {
            teacherService.inicializarProfes();
            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success("Base inicializada correctamente!"));

        } catch (TeacherException error) {
            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.error(error.getMessage()));
        } catch (Exception error) {
            System.out.printf("[TeacherController.initializeTeachersDB] -> %s", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ResponseData.error("No se pudo inicializar la DB"));
        }
  }  
    
    @PostMapping("/createTeacher")
    public ResponseEntity<ResponseData<?>> createTeacher(@RequestBody TeacherDTO teacherDTO) {
         try {
            teacherDTO.setId(null);

            Teacher profe = teacherDTO.toEntity();

            Teacher createdTeacher = teacherService.saveTeacher(profe);

            TeacherDTO createdTeacherDTO = createdTeacher.toDTO();

            return ResponseEntity.status(HttpStatus.CREATED).body(ResponseData.success(createdTeacherDTO));

        } catch (Exception error) {
        System.out.printf("[TeacherController.createTeacher] -> %s", error.getMessage() );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("No se pudo crear el profe"));
        }
    }

    @PutMapping("")
    public ResponseEntity<ResponseData<?>> updateTeacher(@RequestBody TeacherDTO teacherDTO) {
        try {
        Teacher profe = teacherDTO.toEntity();
        Teacher updatedTeacher = teacherService.updateTeacher(profe);
        TeacherDTO updatedTeacherDTO = updatedTeacher.toDTO();
        return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(updatedTeacherDTO));

        }catch (TeacherException error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseData.error(error.getMessage()));

        } catch (Exception error) {
        System.out.printf("[TeacherController.updateTeacher] -> %s", error.getMessage() );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("No se pudo actualizar la info del profe"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<?>> deleteTeacher(@PathVariable Long id) {
        try {
            teacherService.deleteTeacher(id);

            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(null));

        } catch (Exception error) {
        System.out.printf("[TeacherController.deleteTeacher] -> %s", error.getMessage() );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("No se pudo eliminar al profe"));
        }
    }

    @PutMapping("/{teacherId}/{courseId}")
    public ResponseEntity<ResponseData<?>> cargarProfesParaCurso(@PathVariable("profeId") Long profeId, @PathVariable("courseId") Long courseId) {
        try {

            teacherService.cargarProfe(profeId, courseId);
            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(null));

        }catch (TeacherException error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseData.error(error.getMessage()));

        } catch (Exception error) {
        System.out.printf("[TeacherController.cargarProfesParaCurso] -> %s", error.getMessage() );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("No se pudo seleccionar al profe"));
        }
    }
    
}

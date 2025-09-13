package com.example.g7_back_mobile.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.g7_back_mobile.repositories.CourseRepository;
import com.example.g7_back_mobile.repositories.TeacherRepository;
import com.example.g7_back_mobile.repositories.entities.Course;
import com.example.g7_back_mobile.repositories.entities.Teacher;
import com.example.g7_back_mobile.services.exceptions.CourseException;
import com.example.g7_back_mobile.services.exceptions.TeacherException;

import jakarta.transaction.Transactional;

@Service
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;


    public List<Teacher> todosLosProfes() throws TeacherException {
			List<Teacher> teachers = teacherRepository.findAll();
			if (teachers.isEmpty()) {
				throw new TeacherException("No se encontraron profes en la base de datos.");
			}
			return teachers;
	}

    public Teacher updateTeacher(Teacher teacher) throws Exception {
          try {
            if (!teacherRepository.existsById(teacher.getId())) 
              throw new TeacherException("el profe con id: '" + teacher.getId() + "' no existe.");
            
            Teacher updatedTeacher = teacherRepository.save(teacher);
            return updatedTeacher;
          } catch (TeacherException error) {
            throw new TeacherException(error.getMessage());
          } catch (Exception error) {
            throw new Exception("[Service.updateTeacher] -> " + error.getMessage());
          }
    }

	public Teacher saveTeacher(Teacher teacher) throws Exception {
		try{
        Teacher profe = teacherRepository.save(teacher);   
        return profe;

		} catch (Exception error) {
            throw new Exception("[Service.saveTeacher] -> " + error.getMessage());
          }
        }
    
    public void inicializarProfes() throws Exception {
		try{	
            Teacher teacher1 = new Teacher(null, "María Lopez");
            Teacher teacher2 = new Teacher(null, "Hernan Satō");
            Teacher teacher3 = new Teacher(null, "Laura Martinez");

            teacherRepository.save(teacher1); 
			teacherRepository.save(teacher2);
			teacherRepository.save(teacher3);

		 } catch (TeacherException error) {

        	throw new TeacherException(error.getMessage());
      } catch (Exception error) {
				throw new Exception("[Service.inicializarProfes] -> " + error.getMessage());
			}
    }    

	@Transactional
    public void deleteTeacher(Long id) throws Exception {
          try {
              teacherRepository.deleteById(id);
          } catch (Exception error) {
            throw new Exception("[Service.deleteTeacher] -> " + error.getMessage());
          }
    }

	public void cargarProfe(Long teacherId, Long courseId){
		Teacher profeSeleccionado = teacherRepository.findById(teacherId).orElseThrow(() -> new TeacherException("El profe con id " + teacherId + " no existe."));
		Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseException("Curso no encontrado"));
		
		List<Teacher> teachers = new ArrayList<>();
		List<Teacher> existeListaProfes = course.getTeachers();
		if( existeListaProfes == null){
			teachers.add(profeSeleccionado);
			course.setTeachers(teachers);
		} else existeListaProfes.add(profeSeleccionado);
		courseRepository.save(course);
		
		courseRepository.save(course);
	}
    
}

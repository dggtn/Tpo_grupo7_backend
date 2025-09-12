package com.example.g7_back_mobile.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.g7_back_mobile.repositories.CourseRepository;
import com.example.g7_back_mobile.repositories.HeadquarterRepository;
import com.example.g7_back_mobile.repositories.entities.Course;
import com.example.g7_back_mobile.repositories.entities.Headquarter;
import com.example.g7_back_mobile.services.exceptions.CourseException;
import com.example.g7_back_mobile.services.exceptions.HeadquarterException;

import jakarta.transaction.Transactional;

@Service
public class HeadquarterService {

    @Autowired
    private HeadquarterRepository headquarterRepository;

    @Autowired
    private CourseRepository courseRepository;


    public List<Headquarter> todosLasSedes() throws HeadquarterException {
			List<Headquarter> sedes = headquarterRepository.findAll();
			if (sedes.isEmpty()) {
				throw new HeadquarterException("No se encontraron sedes en la base de datos.");
			}
			return sedes;
	}

	
	public Headquarter getHeadquarterByName(String name) throws Exception {
		try{
			return headquarterRepository.findByName(name).orElseThrow(() -> new HeadquarterException("Sede no encontrada"));
		} catch (HeadquarterException error) {
			throw new HeadquarterException(error.getMessage());
		} catch (Exception error) {
			throw new Exception("[Controlador.getCourseByName] -> " + error.getMessage());
		}
	}

      
    public Headquarter updateSede(Headquarter headquarter) throws Exception {
          try {
            if (!headquarterRepository.existsById(headquarter.getId())) 
              throw new HeadquarterException("la sede con id: '" + headquarter.getId() + "' no existe.");
            
            Headquarter updatedSede = headquarterRepository.save(headquarter);
            return updatedSede;
          } catch (HeadquarterException error) {
            throw new HeadquarterException(error.getMessage());
          } catch (Exception error) {
            throw new Exception("[Controlador.updateSede] -> " + error.getMessage());
          }
    }

	public Headquarter saveSede(Headquarter headquarter) throws Exception {
		try{
        Headquarter sede = headquarterRepository.save(headquarter);   
        return sede;

		} catch (Exception error) {
            throw new Exception("[Controlador.saveSede] -> " + error.getMessage());
          }
        }
    

	@Transactional
    public void deleteSede(Long id) throws Exception {
          try {
              headquarterRepository.deleteById(id);
          } catch (Exception error) {
            throw new Exception("[Controlador.deleteSede] -> " + error.getMessage());
          }
    }

	public void cargarSede(Long sedeId, Long courseId){
		Headquarter sedeSeleccionada = headquarterRepository.findById(sedeId).orElseThrow(() -> new HeadquarterException("La sede con id " + sedeId + " no existe."));
		Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseException("Curso no encontrado"));
		
		List<Headquarter> sedes = new ArrayList<>();
		List<Headquarter> existeListaSedes = course.getSedes();
		if( existeListaSedes == null){
			sedes.add(sedeSeleccionada);
			course.setSedes(sedes);
		} else existeListaSedes.add(sedeSeleccionada);
		courseRepository.save(course);
		
		courseRepository.save(course);
	}
}

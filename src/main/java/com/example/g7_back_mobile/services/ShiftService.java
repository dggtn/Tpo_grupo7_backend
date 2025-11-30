package com.example.g7_back_mobile.services;

import com.example.g7_back_mobile.controllers.dtos.CreateShiftRequest;
import com.example.g7_back_mobile.controllers.dtos.RateRequest;
import com.example.g7_back_mobile.controllers.dtos.ShiftRatingDto;
import com.example.g7_back_mobile.repositories.*;
import com.example.g7_back_mobile.repositories.entities.*;
import com.example.g7_back_mobile.services.exceptions.CourseException;
import com.example.g7_back_mobile.services.exceptions.HeadquarterException;
import com.example.g7_back_mobile.services.exceptions.ShiftException;
import com.example.g7_back_mobile.services.exceptions.TeacherException;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShiftService {

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private HeadquarterRepository headquarterRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ShiftRatingRepository shiftRatingRepository;

    @Autowired
    private UserService userService;

     public Shift updateShift(Shift shift) throws Exception {
         try {
            if (!shiftRepository.existsById(shift.getId())) 
              throw new ShiftException("El cronograma con id: '" + shift.getId() + "' no existe.");
            
            Shift updatedShift = shiftRepository.save(shift);
            return updatedShift;
          } catch (ShiftException error) {
            throw new ShiftException(error.getMessage());
          } catch (Exception error) {
            throw new Exception("[ShiftService.updateShift] -> " + error.getMessage());
          }
    }

    public Shift saveCronograma(long courseId, Long sedeId, Long teacherId, CreateShiftRequest request) throws Exception {
      
      Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> new CourseException("Curso no encontrado con ID: " + courseId));

      Headquarter sede = headquarterRepository.findById(sedeId)
        .orElseThrow(() -> new HeadquarterException("Sede no encontrada con ID: " + sedeId));

      if (!course.getSedes().contains(sede)) {
        throw new Exception("La sede con ID " + sedeId + " no está asignada a este curso.");
      }

      Teacher teacher = teacherRepository.findById(teacherId)
			.orElseThrow(() -> new TeacherException("Profesor no encontrada con ID: " + teacherId));

        if (!course.getTeachers().contains(teacher)) {
			throw new Exception("El profesor con ID " + teacherId + " no está asignada a este curso.");
		}

		Shift newSchedule = Shift.builder()
				.clase(course)
				.sede(sede)
        .teacher(teacher)
				.horaInicio(request.getHoraInicio())
				.horaFin(request.getHoraFin())
				.vacancy(request.getVacancy())
				.diaEnQueSeDicta(request.getDiaEnQueSeDicta())
				.build();

		return shiftRepository.save(newSchedule);
	}

    public Shift updateCronograma(Shift courseSchedule) throws Exception {
          try {
            if (!shiftRepository.existsById(courseSchedule.getId())) 
              throw new ShiftException("El cronograma con id: '" + courseSchedule.getId() + "' no existe.");
            
            Shift updatedCourseSched = shiftRepository.save(courseSchedule);
            return updatedCourseSched;
          } catch (ShiftException error) {
            throw new ShiftException(error.getMessage());
          } catch (Exception error) {
            throw new Exception("[Controlador.updateCourseSchedule] -> " + error.getMessage());
          }
    }
    

	@Transactional
    public void deleteCourseSchedule(Long id) throws Exception {
          try {
              
			  shiftRepository.findById(id).orElseThrow(() -> new ShiftException("El cronograma con id " + id + " no existe."));
			  shiftRepository.deleteById(id);
			
          } catch (Exception error) {
            throw new Exception("[Controlador.deleteCourseSchedule] -> " + error.getMessage());
          }
        }

	public List<Shift> findSchedByCourse(Long courseId) throws Exception {
    try{
        return shiftRepository.findByClaseId(courseId);
      } catch(Exception error){
        throw new Exception("[ShiftService.findSchedByCourse] -> " + error.getMessage());
      }
     }

    public List<Shift> findAll(List<Specification<Shift>> specifications) {

        Specification<Shift> specification = null;
        if (!specifications.isEmpty()) {
            specification = specifications.get(0);
            for(int i = 1; i < specifications.size(); i++) {
                specification = specification.and(specifications.get(i));
            }
        }

        return this.shiftRepository.findAll(specification);
    }

    public ShiftRatingDto rate(RateRequest rateRequest) {

         User user = userService.getUserByEmail(rateRequest.getUserEmail());
         Shift shift = Shift.builder().id((rateRequest.getShiftId())).build();

         ShiftRating shiftRating = ShiftRating.builder()
                 .user(user)
                 .shift(shift)
                 .rating(rateRequest.getRating())
                 .comment(rateRequest.getComment())
                 .build();

         shiftRatingRepository.save(shiftRating);

         return ShiftRatingDto.builder().valor(shiftRating.getRating()).build();
    }
}
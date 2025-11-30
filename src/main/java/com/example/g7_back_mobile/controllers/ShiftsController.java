package com.example.g7_back_mobile.controllers;

import com.example.g7_back_mobile.controllers.dtos.*;
import com.example.g7_back_mobile.repositories.ShiftSpecification;
import com.example.g7_back_mobile.repositories.entities.Course;
import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.services.CourseService;
import com.example.g7_back_mobile.services.ShiftService;
import com.example.g7_back_mobile.services.UserService;
import com.example.g7_back_mobile.services.exceptions.ShiftException;
import com.example.g7_back_mobile.services.exceptions.UserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/shifts")
public class ShiftsController {

    @Autowired
    private ShiftService shiftService;
    @Autowired
    private CourseService courseService;

    @GetMapping("")
    public ResponseEntity<ResponseData<?>> getAll(
            @RequestParam(value = "sede", required = false) Long sede,
            @RequestParam(value = "tipoDeporte", required = false) Long tipoDeporte,
            @RequestParam(value = "inicio", required = false) String horaInicio
    ) {
        try {

            List<Specification<Shift>> specifications = new ArrayList<>();
            if (sede != null) {
                specifications.add(new ShiftSpecification(ShiftSpecification.Campo.SEDE, sede));
            }
            if (tipoDeporte != null) {
                specifications.add(new ShiftSpecification(ShiftSpecification.Campo.TIPO_DEPORTE, tipoDeporte));
            }
            if (horaInicio != null) {
                specifications.add(new ShiftSpecification(ShiftSpecification.Campo.HORA_INICIO, horaInicio));
            }

            List<Shift> clases = this.shiftService.findAll(specifications);

            List<ClaseDTO> resultado = new LinkedList<>();
            clases.forEach(shift -> {
                ClaseDTO clase = new ClaseDTO(
                        shift.getClase().getId(),
                        shift.getId(),
                        shift.getSede().getName(),
                        shift.getClase().getName(),
                        shift.getHoraInicio(),
                        shift.getClase().getSportName().getSportTypeName()
                );
                resultado.add(clase);
            });

            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(resultado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseData.error(e.getMessage()));
        }
    }

    @PostMapping("/shift/{courseId}/{sedeId}/{teacherId}")
    public ResponseEntity<ResponseData<?>> createCourseSchedule(
            @PathVariable Long courseId,
            @PathVariable Long sedeId,
            @PathVariable Long teacherId,
            @RequestBody CreateShiftRequest request) {
        try {
            Shift newSchedule = shiftService.saveCronograma(courseId, sedeId, teacherId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ResponseData.success(newSchedule));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseData.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<?>> deleteCourseSchedule(@PathVariable Long id) {
        try {
        shiftService.deleteCourseSchedule(id);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(null));

        } catch (Exception error) {
        System.out.printf("[ApiCourseSchedule.deleteCourseSchedule] -> %s", error.getMessage() );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("No se pudo eliminar el cronograma"));
        }
    }

    @GetMapping("/by-course/{courseId}")
    public ResponseEntity<ResponseData<?>> getCourseShifts(@PathVariable Long courseId) {
        try {

        Course clase = courseService.getCourseById(courseId);

        List<ShiftDTO> shifts = shiftService.findSchedByCourse(clase.getId()).stream().map(Shift::toDTO).toList();

        return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(shifts));

        } catch (UserException error) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ResponseData.error(error.getMessage()));

        } catch (Exception error) {
        System.out.printf("[ShiftController.getCourseShifts] -> %s", error.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ResponseData.error("No se pudieron obtener las compras."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<?>> updateShift(@RequestBody ShiftDTO shiftDTO) {
         try {
            Shift shift = shiftDTO.toEntity();
            Shift updatedShift = shiftService.updateShift(shift);
            ShiftDTO updatedShiftDTO = updatedShift.toDTO();
            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(updatedShiftDTO));

        }catch (ShiftException error) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseData.error(error.getMessage()));

        } catch (Exception error) {
            System.out.printf("[ShiftController.updateShift] -> %s", error.getMessage() );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("No se pudo actualizar el libro"));
        }
    
    }

    @PostMapping("{id}/rating")
    public ResponseEntity<ResponseData<?>> rate(@RequestBody RateShiftRequest request,
                                                @PathVariable("id") Long shiftId,
                                                @AuthenticationPrincipal UserDetails userDetails) {

        RateRequest rateRequest = RateRequest.builder()
                .userEmail(userDetails.getUsername())
                .ShiftId(shiftId)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        ShiftRatingDto response = shiftService.rate(rateRequest);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(response));
    }
}
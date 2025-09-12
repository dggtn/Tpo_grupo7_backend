package com.example.g7_back_mobile.controllers;

import com.example.g7_back_mobile.controllers.dtos.ShiftDTO;
import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.services.ShiftService;
import com.example.g7_back_mobile.services.exceptions.ShiftException;
import com.example.g7_back_mobile.controllers.dtos.CreateShiftRequest;
import com.example.g7_back_mobile.controllers.dtos.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shifts")
public class ShiftsController {

    @Autowired
    private ShiftService shiftService;

    @GetMapping("/available")
    public ResponseEntity<ResponseData<?>> getAvailableShifts() {
        try {
            List<ShiftDTO> availableShifts = shiftService.getAvailableShifts();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseData.success(availableShifts));
        } catch (Exception error) {
            System.out.printf("[ShiftsController.getAvailableShifts] -> %s", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseData.error("No se pudieron obtener los turnos disponibles"));
        }
    }

    @PostMapping("/CourseSchedule/{courseId}/{sedeId}")
    public ResponseEntity<?> createCourseSchedule(
            @PathVariable Long courseId,
            @PathVariable Long sedeId,
            @PathVariable Long teacherId,
            @RequestBody CreateShiftRequest request) {
        try {
            Shift newSchedule = shiftService.saveCronograma(courseId, sedeId, teacherId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(newSchedule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
    public ResponseEntity<List<ShiftDTO>> getSchedulesByCourse(@PathVariable("courseId") Long courseId) {
        try {
            List<ShiftDTO> schedules = shiftService.findSchedByCourse(courseId);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
}
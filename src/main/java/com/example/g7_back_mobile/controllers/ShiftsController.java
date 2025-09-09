package com.example.g7_back_mobile.controllers;

import com.example.g7_back_mobile.controllers.dtos.ShiftsDTO;
import com.example.g7_back_mobile.services.ShiftService;
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
            List<ShiftsDTO> availableShifts = shiftService.getAvailableShifts();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseData.success(availableShifts));
        } catch (Exception error) {
            System.out.printf("[ShiftsController.getAvailableShifts] -> %s", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseData.error("No se pudieron obtener los turnos disponibles"));
        }
    }

    @PostMapping
    public ResponseEntity<ResponseData<?>> createShift(@RequestBody ShiftsDTO shiftDTO) {
        try {
            ShiftsDTO createdShift = shiftService.createShift(shiftDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseData.success(createdShift));
        } catch (Exception error) {
            System.out.printf("[ShiftsController.createShift] -> %s", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseData.error("No se pudo crear el turno"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<?>> updateShift(@PathVariable Long id, @RequestBody ShiftsDTO shiftDTO) {
        try {
            ShiftsDTO updatedShift = shiftService.updateShift(id, shiftDTO);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseData.success(updatedShift));
        } catch (Exception error) {
            System.out.printf("[ShiftsController.updateShift] -> %s", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseData.error("No se pudo actualizar el turno"));
        }
    }
}
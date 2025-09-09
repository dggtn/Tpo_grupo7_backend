package com.example.g7_back_mobile.services;

import com.example.g7_back_mobile.controllers.dtos.ShiftsDTO;
import com.example.g7_back_mobile.repositories.ShiftRepository;
import com.example.g7_back_mobile.repositories.entities.Shift;
import com.example.g7_back_mobile.repositories.entities.Teacher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShiftService {

    @Autowired
    private ShiftRepository shiftRepository;

    public List<ShiftsDTO> getAvailableShifts() {
        List<Shift> shifts = shiftRepository.findAll();
        return shifts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ShiftsDTO convertToDTO(Shift shift) {
        ShiftsDTO dto = new ShiftsDTO();

        dto.setShift(String.valueOf(shift.getId()));

        // Mapear sede
        if (shift.getSede() != null) {
            dto.setSede(shift.getSede().getName());
        }

        // Mapear disciplina (asumiendo que está en la clase)
        if (shift.getClase() != null) {
            dto.setDisciplina(shift.getClase().getName());
            dto.setClassName(shift.getClase().getName());
        }

        // Convertir LocalDateTime a LocalDate
        if (shift.getLocalTime() != null) {
            dto.setFecha(shift.getLocalTime().toLocalDate());
        }

        // Mapear deportes
        if (shift.getSportType() != null) {
            dto.setSports(shift.getSportType().getSportTypeName());
        }

        // Mapear profesores
        if (shift.getTeachers() != null && !shift.getTeachers().isEmpty()) {
            String teachersStr = shift.getTeachers().stream()
                    .map(Teacher::getName)
                    .collect(Collectors.joining(", "));
            dto.setTeachers(teachersStr);
        }

        return dto;
    }

    public ShiftsDTO updateShift(Long id, ShiftsDTO shiftDTO) {
        // Verificar si el turno existe
        Shift existingShift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado con id: " + id));

        // Actualizar los datos del turno existente
        updateShiftData(existingShift, shiftDTO);

        // Guardar los cambios
        Shift updatedShift = shiftRepository.save(existingShift);

        // Convertir la entidad actualizada a DTO
        return convertToDTO(updatedShift);
    }

    private void updateShiftData(Shift shift, ShiftsDTO dto) {
        // Actualizar fecha si se proporciona
        if (dto.getFecha() != null) {
            // Preservar la hora original si es posible
            LocalDateTime currentTime = shift.getLocalTime();
            int hour = 0, minute = 0;
            if (currentTime != null) {
                hour = currentTime.getHour();
                minute = currentTime.getMinute();
            }
            shift.setLocalTime(dto.getFecha().atTime(hour, minute));
        }

        // Aquí necesitarías repositorios adicionales para buscar entidades relacionadas

        // Ejemplo: Actualizar sede si se proporciona el nombre
        // if (dto.getSede() != null) {
        //     Headquarter headquarter = headquarterRepository.findByName(dto.getSede());
        //     shift.setSede(headquarter);
        // }

        // Actualizar clase/disciplina
        // if (dto.getDisciplina() != null) {
        //     Class clase = classRepository.findByName(dto.getDisciplina());
        //     shift.setClase(clase);
        // }

        // Actualizar tipo de deporte
        // if (dto.getSports() != null) {
        //     Sport sport = sportRepository.findBySportTypeName(dto.getSports());
        //     shift.setSportType(sport);
        // }

        // Para actualizar profesores, necesitarías procesarlos si vienen como una lista
    }

    public ShiftsDTO createShift(ShiftsDTO shiftDTO) {
        // Convertir DTO a entidad
        Shift newShift = new Shift();
        updateShiftData(newShift, shiftDTO);

        // Guardar la entidad
        Shift savedShift = shiftRepository.save(newShift);

        // Convertir la entidad guardada de vuelta a DTO
        return convertToDTO(savedShift);
    }
}
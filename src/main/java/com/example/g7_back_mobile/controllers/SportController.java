package com.example.g7_back_mobile.controllers;
import com.example.g7_back_mobile.controllers.dtos.ResponseData;
import com.example.g7_back_mobile.controllers.dtos.SportDTO;
import com.example.g7_back_mobile.repositories.entities.Sport;
import com.example.g7_back_mobile.services.SportService;
import com.example.g7_back_mobile.services.exceptions.SportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/sports")
public class SportController {
        @Autowired
        private SportService sportService;

    @GetMapping("/allSports")
    public ResponseEntity<ResponseData<?>> obtenerTodosLasDisciplinas() {
        try {
            List<Sport> sports = sportService.getAllSports();
            List <SportDTO> sportDTO0s= sports.stream().map((sport)->new SportDTO(sport.getId(),sport.getSportTypeName())).toList();
            return ResponseEntity.ok(ResponseData.success(sportDTO0s));
        } catch (SportException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseData.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("Error inesperado: " + e.getMessage()));
        }
    }
    }

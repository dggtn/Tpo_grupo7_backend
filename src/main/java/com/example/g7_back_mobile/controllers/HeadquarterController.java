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

import com.example.g7_back_mobile.controllers.dtos.HeadquarterDTO;
import com.example.g7_back_mobile.controllers.dtos.ResponseData;
import com.example.g7_back_mobile.repositories.entities.Headquarter;
import com.example.g7_back_mobile.services.HeadquarterService;
import com.example.g7_back_mobile.services.exceptions.HeadquarterException;

@RestController
@RequestMapping("/headquarters")
public class HeadquarterController {
    @Autowired
    private HeadquarterService headquarterService;

    @GetMapping("/allHeadquarters")
    public ResponseEntity<?> obtenerTodosLasSedes() {
        try {
            List<Headquarter> sedes = headquarterService.todosLasSedes();
            return ResponseEntity.ok(sedes);
        } catch (HeadquarterException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado: " + e.getMessage());
        }
    }

    @PostMapping("/initializeHeadquarters")
    public ResponseEntity<ResponseData<String>> initializeHeadquartersDB() {
        try {
            headquarterService.inicializarSedes();
            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success("Base inicializada correctamente!"));

        } catch (HeadquarterException error) {
            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.error(error.getMessage()));
        } catch (Exception error) {
            System.out.printf("[HeadquarterController.initializeHeadquartersDB] -> %s", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ResponseData.error("No se pudo inicializar la DB"));
        }
  }  
    
    @PostMapping("/createHeadquarter")
    public ResponseEntity<ResponseData<?>> createSede(@RequestBody HeadquarterDTO headquarterDTO) {
         try {
            headquarterDTO.setId(null);

            Headquarter sede = headquarterDTO.toEntity();

            Headquarter createdSede = headquarterService.saveSede(sede);

            HeadquarterDTO createdSedeDTO = createdSede.toDTO();

            return ResponseEntity.status(HttpStatus.CREATED).body(ResponseData.success(createdSedeDTO));

        } catch (Exception error) {
        System.out.printf("[HeadController.createSede] -> %s", error.getMessage() );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("No se pudo crear la sede"));
        }
    }

    @PutMapping("")
    public ResponseEntity<ResponseData<?>> updateSede(@RequestBody HeadquarterDTO headquarterDTO) {
        try {
        Headquarter sede = headquarterDTO.toEntity();
        Headquarter updatedSede = headquarterService.updateSede(sede);
        HeadquarterDTO updatedSedeDTO = updatedSede.toDTO();
        return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(updatedSedeDTO));

        }catch (HeadquarterException error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseData.error(error.getMessage()));

        } catch (Exception error) {
        System.out.printf("[HeadquarterController.updateSede] -> %s", error.getMessage() );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("No se pudo actualizar la sede"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<?>> deleteSede(@PathVariable Long id) {
        try {
        headquarterService.deleteSede(id);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(null));

        } catch (Exception error) {
        System.out.printf("[HeadquarterController.deleteSede] -> %s", error.getMessage() );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("No se pudo eliminar la sede"));
        }
    }

    @PutMapping("/{sedeId}/{courseId}")
    public ResponseEntity<ResponseData<?>> cargarSedesParaCurso(@PathVariable("sedeId") Long sedeId, @PathVariable("courseId") Long courseId) {
        try {

        headquarterService.cargarSede(sedeId, courseId);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(null));

        }catch (HeadquarterException error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseData.error(error.getMessage()));

        } catch (Exception error) {
        System.out.printf("[HeadquarterController.cargarSedesParaCurso] -> %s", error.getMessage() );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.error("No se pudo seleccionar la sede"));
        }
    }
}

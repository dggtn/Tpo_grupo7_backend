package com.example.g7_back_mobile.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import java.io.ByteArrayOutputStream;
import com.example.g7_back_mobile.controllers.dtos.AsistenciaDTO;
import com.example.g7_back_mobile.controllers.dtos.AsistenciaResultadoDTO;
import com.example.g7_back_mobile.controllers.dtos.ResponseData;
import com.example.g7_back_mobile.repositories.entities.CourseAttend;
import com.example.g7_back_mobile.services.CourseAttendService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/asistencias")
@RequiredArgsConstructor
public class CourseAttendController {

    @Autowired
    private CourseAttendService courseAttendService;

    @PostMapping("/registrar_asistencia")
    public ResponseEntity<ResponseData<?>> registrarAsistencia(@RequestBody AsistenciaDTO asistenciaDTO) {
        try {
            System.out.println("[CourseAttendController.registrarAsistencia] Recibiendo petición: " + asistenciaDTO);
            
            CourseAttend asistencia = courseAttendService.registrarAsistencia(asistenciaDTO);
            
            String mensaje = String.format("Asistencia registrada exitosamente para la fecha %s", 
                asistencia.getFechaAsistencia());
                
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseData.success(mensaje));
                
        } catch (IllegalArgumentException e) {
            System.err.println("[CourseAttendController.registrarAsistencia] Error de validación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseData.error("Error de validación: " + e.getMessage()));
                
        } catch (IllegalStateException e) {
            System.err.println("[CourseAttendController.registrarAsistencia] Error de estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseData.error("Error de estado: " + e.getMessage()));
                
        } catch (Exception e) {
            System.err.println("[CourseAttendController.registrarAsistencia] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("Error interno del servidor. Por favor, intente nuevamente."));
        }
    }

    @GetMapping("/inscripcion/{id}/resultado")
    public ResponseEntity<ResponseData<?>> getResultadoAsistencia(@PathVariable Long id) {
        try {
            System.out.println("[CourseAttendController.getResultadoAsistencia] Consultando inscripción ID: " + id);
            
            AsistenciaResultadoDTO resultado = courseAttendService.verificarAsistencia(id);
            
            return ResponseEntity.ok(ResponseData.success(resultado));
            
        } catch (IllegalArgumentException e) {
            System.err.println("[CourseAttendController.getResultadoAsistencia] Error de validación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseData.error("Error de validación: " + e.getMessage()));
                
        } catch (Exception e) {
            System.err.println("[CourseAttendController.getResultadoAsistencia] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("Error interno del servidor. Por favor, intente nuevamente."));
        }
    }

    @GetMapping(value = "/qr/{shiftId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrCode(@PathVariable Long shiftId) {
        try {
            System.out.println("[CourseAttendController.generateQrCode] Generando QR para shift ID: " + shiftId);
            
            if (shiftId == null || shiftId <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            String qrContent = String.valueOf(shiftId);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            System.out.println("[CourseAttendController.generateQrCode] QR generado exitosamente, tamaño: " + pngData.length + " bytes");
            
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(pngData);
                
        } catch (Exception e) {
            System.err.println("[CourseAttendController.generateQrCode] Error generando QR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
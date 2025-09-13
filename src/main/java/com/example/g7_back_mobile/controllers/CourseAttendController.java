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
import com.example.g7_back_mobile.services.CourseAttendService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/Asistencias")
@RequiredArgsConstructor
public class CourseAttendController {

    @Autowired
    private CourseAttendService courseAttendService;

    @PostMapping("/registrar_asistencia")
    public ResponseEntity<?> registrarAsistencia(@RequestBody AsistenciaDTO asistenciaDTO) {
        try {
            // Llamamos a un método en el controlador que crearemos ahora
            courseAttendService.registrarAsistencia(asistenciaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("Asistencia registrada con éxito.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/inscripcion/{id}/resultado")
    public ResponseEntity<?> getResultadoAsistencia(@PathVariable Long id) {
        try {
            AsistenciaResultadoDTO resultado = courseAttendService.verificarAsistencia(id);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping(value = "/qr/{shiftId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrCode(@PathVariable Long shiftId) {
        try {
            String qrContent = String.valueOf(shiftId);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            return ResponseEntity.ok(pngData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
}

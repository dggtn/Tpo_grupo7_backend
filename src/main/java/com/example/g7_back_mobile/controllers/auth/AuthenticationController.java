package com.example.g7_back_mobile.controllers.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.g7_back_mobile.controllers.dtos.ResponseData;
import com.example.g7_back_mobile.services.AuthenticationService;
import com.example.g7_back_mobile.services.exceptions.UserException;

import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
  @Autowired
  private final AuthenticationService authService;

   @PostMapping("/iniciar-registro")
    public ResponseEntity<String> iniciarRegistro(@RequestBody RegisterRequest request) {
        try {
            authService.iniciarRegistro(request);
            return ResponseEntity.ok("Código de verificación enviado al correo.");
        } catch (UserException e) {
            // Usamos HttpStatus.CONFLICT (409) si el usuario o email ya existen
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            // Usamos HttpStatus.INTERNAL_SERVER_ERROR (500) para otros problemas
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al enviar el correo: " + e.getMessage());
        }
    }

    @PostMapping("/finalizar-registro")
    public ResponseEntity<String> finalizarRegistro(@RequestBody VerificationRequest request) {
        try {
            authService.finalizarRegistro(request.getEmail(), request.getCode());
            return ResponseEntity.ok("Usuario registrado y verificado con éxito.");
        } catch (UserException e) {
            // Usamos HttpStatus.BAD_REQUEST (400) si el código es incorrecto, expiró, etc.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado al finalizar el registro: " + e.getMessage());
        }
  }

  @PostMapping("/authenticate")
    public ResponseEntity<ResponseData<?>> authenticate(
        @RequestBody AuthenticationRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseData.success(authService.authenticate(request)));
        } catch (UserException | AuthException error) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseData.error(error.getMessage()));
        } catch (Exception error) {
            System.out.printf("[AuthenticationController.authenticate] -> %s", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("Usuario o contraseña inválido."));
        }
    }
  }

  


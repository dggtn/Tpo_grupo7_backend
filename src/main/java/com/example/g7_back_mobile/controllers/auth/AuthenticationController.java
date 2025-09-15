package com.example.g7_back_mobile.controllers.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
  public ResponseEntity<ResponseData<String>> iniciarRegistro(@RequestBody RegisterRequest request) {
    try {
        authService.iniciarRegistro(request);
        return ResponseEntity.ok(ResponseData.success("Código de verificación enviado al correo."));
    } catch (UserException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ResponseData.error(e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ResponseData.error("Error al enviar el correo: " + e.getMessage()));
    }
  }

  @PostMapping("/finalizar-registro")
  public ResponseEntity<ResponseData<String>> finalizarRegistro(@RequestBody VerificationRequest request) {
    try {
        authService.finalizarRegistro(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ResponseData.success("Usuario registrado y verificado con éxito."));
    } catch (UserException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ResponseData.error(e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ResponseData.error("Error inesperado al finalizar el registro: " + e.getMessage()));
    }
  }

  @Operation(summary = "Autenticar usuario", description = "Permite iniciar sesión a un usuario registrado")
  @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
            @ApiResponse(responseCode = "400", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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




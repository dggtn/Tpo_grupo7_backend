package com.example.g7_back_mobile.controllers.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

  @PostMapping("/reenviar-codigo")
  public ResponseEntity<ResponseData<String>> reenviarCodigo(@RequestBody ReenviarCodigoRequest request) {
        try {
            authService.reenviarCodigoVerificacion(request.getEmail());
            return ResponseEntity.ok(ResponseData.success("Nuevo código de verificación enviado al correo."));
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseData.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("Error al reenviar el código: " + e.getMessage()));
        }
  }

    @PostMapping("/verificar-email-pendiente")
    public ResponseEntity<ResponseData<String>> verificarEmailPendiente(@RequestBody VerificarEmailRequest request) {
        try {
            boolean existe = authService.existeRegistroPendiente(request.getEmail());
            if (existe) {
                return ResponseEntity.ok(ResponseData.success("Registro pendiente encontrado para " + request.getEmail() + ". Puedes solicitar un nuevo código."));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST) // Cambiar a BAD_REQUEST
                    .body(ResponseData.error("No se encontró un registro pendiente para " + request.getEmail() + ". Puedes iniciar un nuevo registro."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("Error al verificar el email: " + e.getMessage()));
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

  @Operation(summary = "Cerrar sesión", description = "Permite cerrar la sesión del usuario autenticado")
  @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout exitoso"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
  @PostMapping("/logout")
    public ResponseEntity<ResponseData<String>> logout(
        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Con JWT stateless, el logout se maneja principalmente en el cliente
            // El servidor puede realizar acciones como logging, limpiar caché, etc.
            String username = userDetails != null ? userDetails.getUsername() : "Usuario desconocido";
            System.out.printf("[AuthenticationController.logout] -> Usuario %s ha cerrado sesión%n", username);
            
            return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseData.success("Sesión cerrada exitosamente"));
        } catch (Exception error) {
            System.out.printf("[AuthenticationController.logout] -> %s", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("Error al cerrar sesión"));
        }
    }
}
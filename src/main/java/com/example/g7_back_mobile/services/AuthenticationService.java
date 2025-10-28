package com.example.g7_back_mobile.services;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.g7_back_mobile.controllers.auth.AuthenticationRequest;
import com.example.g7_back_mobile.controllers.auth.AuthenticationResponse;
import com.example.g7_back_mobile.controllers.auth.RegisterRequest;
import com.example.g7_back_mobile.repositories.entities.PendingUser;
import com.example.g7_back_mobile.repositories.entities.User;
import com.example.g7_back_mobile.services.exceptions.UserException;
import com.example.g7_back_mobile.repositories.PendingUserRepository;
import com.example.g7_back_mobile.repositories.UserRepository;

import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
        @Autowired
        private final UserRepository userRepository;
        @Autowired 
        private final PendingUserRepository pendingUserRepository;
        @Autowired
        private final JwtService jwtService;
        @Autowired
        private final AuthenticationManager authenticationManager;
        @Autowired
        private UserService userService;
        @Autowired
        private final EmailService emailService;
		@Autowired
    	private PasswordEncoder passwordEncoder;

	public boolean existeRegistroPendiente(String email) {
		return pendingUserRepository.existsById(email);
	}

	public void reenviarCodigoVerificacion(String email) throws UserException {
			PendingUser pendingUser = pendingUserRepository.findById(email)
				.orElseThrow(() -> new UserException("No se encontró un registro pendiente para este email. Debes iniciar el proceso de registro nuevamente."));

			LocalDateTime ahora = LocalDateTime.now();

			// Verificar si no ha pasado demasiado tiempo desde la creación inicial (24 horas)
			if (pendingUser.getFechaCreacion() != null && 
				pendingUser.getFechaCreacion().isBefore(ahora.minusHours(24))) {
				pendingUserRepository.delete(pendingUser);
				throw new UserException("El registro pendiente ha expirado completamente. Debes iniciar el proceso de registro nuevamente.");
			}

			// Verificar límite de reenvíos (máximo 5 por registro)
			int intentosActuales = pendingUser.getIntentosReenvio() != null ? pendingUser.getIntentosReenvio() : 0;
			if (intentosActuales >= 5) {
				pendingUserRepository.delete(pendingUser);
				throw new UserException("Has alcanzado el límite máximo de reenvíos de código. Debes iniciar el proceso de registro nuevamente.");
			}

			// Verificar tiempo entre reenvíos (mínimo 2 minutos)
			if (pendingUser.getUltimoReenvio() != null) {
				long minutosDesdeUltimoReenvio = java.time.Duration.between(pendingUser.getUltimoReenvio(), ahora).toMinutes();
				if (minutosDesdeUltimoReenvio < 2) {
					long minutosRestantes = 2 - minutosDesdeUltimoReenvio;
					throw new UserException("Debes esperar " + minutosRestantes + " minutos antes de solicitar un nuevo código.");
				}
			}

			// Generar nuevo código
			String nuevoCodigo = String.format("%04d", new Random().nextInt(10000));
			
			// Actualizar datos
			pendingUser.setVerificationCode(nuevoCodigo);
			pendingUser.setExpiryDate(ahora.plusMinutes(15));
			pendingUser.setIntentosReenvio(intentosActuales + 1);
			pendingUser.setUltimoReenvio(ahora);
			
			// Guardar cambios
			pendingUserRepository.save(pendingUser);
			
			// Enviar nuevo código
			emailService.sendVerificationCodeResend(pendingUser.getEmail(), nuevoCodigo, pendingUser.getIntentosReenvio());
			
			System.out.println("[AuthenticationService] Código reenviado a: " + email + " (Intento #" + pendingUser.getIntentosReenvio() + ")");
		}


        public boolean emailExists(String email) {
		return userRepository.existsByEmail(email);
	}


     //--------Iniciar Registro--------
	public void iniciarRegistro(RegisterRequest request) throws UserException {
    
		if (emailExists(request.getEmail())) {
			throw new UserException("El correo electrónico '" + request.getEmail() + "' ya está registrado.");
		}

		String code = String.format("%04d", new Random().nextInt(10000));
		LocalDateTime ahora = LocalDateTime.now();

		PendingUser pendingUser = PendingUser.builder()
			.username(request.getUsername())
			.email(request.getEmail())
			.password(request.getPassword())
			.firstName(request.getFirstName())
			.lastName(request.getLastName())
			.age(request.getAge())
			.address(request.getAddress())
			.urlAvatar(request.getUrlAvatar())
			.verificationCode(code)
			.expiryDate(ahora.plusMinutes(15))
			.fechaCreacion(ahora) // NUEVO CAMPO
			.intentosReenvio(0)   // NUEVO CAMPO
			.ultimoReenvio(ahora) // NUEVO CAMPO
			.build();

		pendingUserRepository.save(pendingUser);
		emailService.sendVerificationCode(pendingUser.getEmail(), code);
	}

	//--------Finalizar Registro--------
	public void finalizarRegistro(String email, String code) throws Exception {
		PendingUser pendingUser = pendingUserRepository.findById(email)
			.orElseThrow(() -> new UserException("No se encontró un registro pendiente para este email. Puede que haya expirado o necesites iniciar el registro nuevamente."));

		// Verificar si el registro completo ha expirado (24 horas desde creación)
		if (pendingUser.getFechaCreacion() != null && 
			pendingUser.getFechaCreacion().isBefore(LocalDateTime.now().minusHours(24))) {
			pendingUserRepository.delete(pendingUser);
			throw new UserException("El registro pendiente ha expirado completamente. Debes iniciar el proceso de registro nuevamente.");
		}

		// Verificar si el código actual ha expirado
		if (pendingUser.getExpiryDate().isBefore(LocalDateTime.now())) {
			throw new UserException("El código de verificación ha expirado. Puedes solicitar un nuevo código usando la opción 'Reenviar código'.");
		}

		if (!pendingUser.getVerificationCode().equals(code)) {
			throw new UserException("El código de verificación es incorrecto.");
		}

		// Proceder con la creación del usuario
		RegisterRequest finalRequest = new RegisterRequest();
		finalRequest.setUsername(pendingUser.getUsername());
		finalRequest.setEmail(pendingUser.getEmail());
		finalRequest.setPassword(pendingUser.getPassword());
		finalRequest.setFirstName(pendingUser.getFirstName());
		finalRequest.setLastName(pendingUser.getLastName());
		finalRequest.setAge(pendingUser.getAge());
		finalRequest.setAddress(pendingUser.getAddress());
		finalRequest.setUrlAvatar(pendingUser.getUrlAvatar());
		
		userService.createUser(finalRequest); 
		pendingUserRepository.delete(pendingUser);
		
		System.out.println("[AuthenticationService] Usuario registrado exitosamente: " + email);
	}

	//--------Autenticar Usuario--------
    public AuthenticationResponse authenticate(AuthenticationRequest request) throws Exception {
                try{
                        authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                        request.getEmail(),
                                        request.getPassword()));
                User user = userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new UserException("El usuario " + request.getEmail() + " no existe."));
                
                String jwtToken = jwtService.generateToken(user);
                return AuthenticationResponse.builder()
                        .accessToken(jwtToken)
                        .build();
                } catch (AuthenticationException error){
                        System.out.printf("[AuthenticationService.authenticate] -> %s", error.getMessage());
                        throw new AuthException("Usuario o contraseña incorrecto.");
                }catch (UserException error) {
                        throw new UserException(error.getMessage());
                }catch (Exception error) {
                        throw new Exception("[AuthenticationService.authenticate] -> " + error.getMessage());
                }
        }

	//----Iniciar recuperación de contraseña----
	
	public void iniciarRecuperacionContrasena(String email) throws UserException {
		System.out.println("[AuthService] Iniciando recuperación para: " + email);
		
		// Verificar que el usuario exista
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new UserException("No existe una cuenta con el email: " + email));
		
		// Generar código de verificación
		String code = String.format("%04d", new Random().nextInt(10000));
		LocalDateTime ahora = LocalDateTime.now();
		
		// Crear o actualizar PendingUser para recuperación
		PendingUser pendingReset = PendingUser.builder()
			.email(email)
			.verificationCode(code)
			.expiryDate(ahora.plusMinutes(15))
			.fechaCreacion(ahora)
			.ultimoReenvio(ahora)
			.intentosReenvio(0)
			.operationType(PendingUser.OperationType.PASSWORD_RESET) // ✅ CRÍTICO
			.build();
		
		pendingUserRepository.save(pendingReset);
		
		// Enviar email con código
		emailService.sendPasswordResetCode(email, code, user.getFirstName());
		
		System.out.println("[AuthService] Código de recuperación enviado a: " + email);
	}

	
	//----Verificar código de recuperación de contraseña----
	
	public void verificarCodigoRecuperacion(String email, String code) throws UserException {
		System.out.println("[AuthService] Verificando código para: " + email);
		
		PendingUser pendingReset = pendingUserRepository.findById(email)
			.orElseThrow(() -> new UserException("No se encontró una solicitud de recuperación para este email."));
		
		// Verificar que sea una recuperación de contraseña
		if (pendingReset.getOperationType() != PendingUser.OperationType.PASSWORD_RESET) {
			throw new UserException("Esta solicitud no es de recuperación de contraseña.");
		}
		
		// Verificar expiración (15 minutos)
		if (pendingReset.getExpiryDate().isBefore(LocalDateTime.now())) {
			throw new UserException("El código de verificación ha expirado. Solicita uno nuevo.");
		}
    
		// Verificar código
		if (!pendingReset.getVerificationCode().equals(code)) {
			throw new UserException("El código de verificación es incorrecto.");
		}
		
		System.out.println("[AuthService] Código verificado exitosamente para: " + email);
	}

	//------Resetear contraseña-------
	
	public void resetearContrasena(String email, String code, String newPassword) throws UserException {
		System.out.println("[AuthService] Reseteando contraseña para: " + email);
		
		// Verificar código nuevamente
		verificarCodigoRecuperacion(email, code);
		
		// Buscar usuario
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new UserException("Usuario no encontrado."));
		
		// Actualizar contraseña
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		
		// Limpiar solicitud de recuperación
		pendingUserRepository.deleteById(email);
		
		System.out.println("[AuthService] Contraseña actualizada exitosamente para: " + email);
	}

	//----OPCIONAL: Reenviar código de recuperación de contraseña----

	public void reenviarCodigoRecuperacion(String email) throws UserException {
				System.out.println("[AuthService] Reenviando código de recuperación a: " + email);
			
			PendingUser pendingReset = pendingUserRepository.findById(email)
				.orElseThrow(() -> new UserException("No se encontró una solicitud de recuperación para este email."));
			
			// Verificar que sea recuperación
			if (pendingReset.getOperationType() != PendingUser.OperationType.PASSWORD_RESET) {
				throw new UserException("Esta solicitud no es de recuperación de contraseña.");
			}
    
   			 // Verificar límite de tiempo (2 minutos entre reenvíos)
			if (pendingReset.getUltimoReenvio() != null) {
				long minutosDesdeUltimoReenvio = java.time.Duration.between(
					pendingReset.getUltimoReenvio(), 
					LocalDateTime.now()
				).toMinutes();
				
				if (minutosDesdeUltimoReenvio < 2) {
					throw new UserException("Debes esperar " + (2 - minutosDesdeUltimoReenvio) + 
						" minutos antes de solicitar un nuevo código.");
				}
			}
    
			// Verificar límite de reenvíos
			int intentos = pendingReset.getIntentosReenvio() != null ? pendingReset.getIntentosReenvio() : 0;
			if (intentos >= 5) {
				pendingUserRepository.delete(pendingReset);
				throw new UserException("Has alcanzado el límite de reenvíos. Inicia el proceso nuevamente.");
			}
    
			// Generar nuevo código
			String nuevoCodigo = String.format("%04d", new Random().nextInt(10000));
			LocalDateTime ahora = LocalDateTime.now();
			
			pendingReset.setVerificationCode(nuevoCodigo);
			pendingReset.setExpiryDate(ahora.plusMinutes(15));
			pendingReset.setIntentosReenvio(intentos + 1);
			pendingReset.setUltimoReenvio(ahora);
			
			pendingUserRepository.save(pendingReset);
			
			// Buscar nombre del usuario
			User user = userRepository.findByEmail(email).orElse(null);
			String firstName = user != null ? user.getFirstName() : null;
			
			emailService.sendPasswordResetCodeResend(email, nuevoCodigo, intentos + 1, firstName);
			
			System.out.println("[AuthService] Código reenviado a: " + email + " (Intento #" + (intentos + 1) + ")");
		}
}

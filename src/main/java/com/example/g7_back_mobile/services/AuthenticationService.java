package com.example.g7_back_mobile.services;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
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

        public boolean emailExists(String email) {
		return userRepository.existsByEmail(email);
	}


        //--------Iniciar Registro--------
	public void iniciarRegistro(RegisterRequest request) throws UserException {
		
		if (emailExists(request.getEmail())) {
			throw new UserException("El correo electrónico '" + request.getEmail() + "' ya está registrado.");
		}

		String code = String.format("%04d", new Random().nextInt(10000));

		PendingUser pendingUser = PendingUser.builder()
				.username(request.getUsername()).email(request.getEmail()).password(request.getPassword())
				.firstName(request.getFirstName()).lastName(request.getLastName())
				.verificationCode(code)
				.expiryDate(LocalDateTime.now().plusMinutes(15))
				.build();

		pendingUserRepository.save(pendingUser);

		emailService.sendVerificationCode(pendingUser.getEmail(), code);
	}

	//--------Finalizar Registro--------
	public void finalizarRegistro(String email, String code) throws Exception {
		PendingUser pendingUser = pendingUserRepository.findById(email)
				.orElseThrow(() -> new UserException("No se encontró un registro pendiente para este email. Puede que haya expirado."));

		if (pendingUser.getExpiryDate().isBefore(LocalDateTime.now())) {
			pendingUserRepository.delete(pendingUser);
			throw new UserException("El código de verificación ha expirado. Por favor, intenta registrarte de nuevo.");
		}

		if (!pendingUser.getVerificationCode().equals(code)) {
			throw new UserException("El código de verificación es incorrecto.");
		}

		RegisterRequest finalRequest = new RegisterRequest();
		finalRequest.setUsername(pendingUser.getUsername());
		finalRequest.setEmail(pendingUser.getEmail());
		finalRequest.setPassword(pendingUser.getPassword());
		finalRequest.setFirstName(pendingUser.getFirstName());
		finalRequest.setLastName(pendingUser.getLastName());

		userService.createUser(finalRequest); 

		pendingUserRepository.delete(pendingUser);
	}


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
}

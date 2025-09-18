package com.example.g7_back_mobile.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.g7_back_mobile.controllers.auth.RegisterRequest;
import com.example.g7_back_mobile.repositories.UserRepository;
import com.example.g7_back_mobile.repositories.entities.Role;
import com.example.g7_back_mobile.repositories.entities.User;
import com.example.g7_back_mobile.services.exceptions.UserException;

import jakarta.transaction.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getUserByEmail(String email) throws UserException {
        try {
            System.out.println("[UserService.getUserByEmail] Buscando usuario con email: " + email);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserException("Usuario no encontrado con email: " + email));
            System.out.println("[UserService.getUserByEmail] Usuario encontrado: " + user.getEmail());
            return user;
        } catch (UserException error) {
            System.err.println("[UserService.getUserByEmail] Usuario no encontrado: " + error.getMessage());
            throw error;
        } catch (Exception error) {
            System.err.println("[UserService.getUserByEmail] Error inesperado: " + error.getMessage());
            throw new UserException("[UserService.getUserByEmail] -> " + error.getMessage());
        }
    }

    public User getUserByUsername(String username) throws UserException {
        try {
            System.out.println("[UserService.getUserByUsername] Buscando usuario con username: " + username);
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserException("Usuario no encontrado con username: " + username));
        } catch (UserException error) {
            throw error;
        } catch (Exception error) {
            throw new UserException("[UserService.getUserByUsername] -> " + error.getMessage());
        }
    }

    @Transactional
    public User createUser(RegisterRequest request) {
        System.out.println("[UserService.createUser] Creando usuario con email: " + request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email '" + request.getEmail() + "' already exists");
        }
        User user = new User(
            null,
            request.getUsername(),
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            Role.USER,
            request.getAge(),
            request.getAddress(),
            request.getUrlAvatar()
        );

        User savedUser = userRepository.save(user);
        System.out.println("[UserService.createUser] Usuario creado exitosamente con ID: " + savedUser.getId());
        return savedUser;
    }

    public User updateUser(User user) throws Exception {
        try {
            return userRepository.save(user);
        } catch (Exception error) {
            throw new Exception("[UserService.updateUser] -> " + error.getMessage());
        }
    }

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
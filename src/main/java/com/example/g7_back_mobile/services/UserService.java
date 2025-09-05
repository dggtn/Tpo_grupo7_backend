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

  
     public User getUserByUsername(String username) throws Exception {
        try {
          return userRepository.findByUsername(username).orElseThrow(() -> new UserException("Usuario no encontrado"));
        } catch (UserException error) {
          throw new UserException(error.getMessage());
        } catch (Exception error) {
          throw new Exception("[UserService.getUserByUsername] -> " + error.getMessage());
        }
    }

   
	@Transactional
    public User createUser(RegisterRequest request) {
        
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
        
        return userRepository.save(user);
    }
    
   public User updateUser(User user) throws Exception {
        try {
          return userRepository.save(user);
        } catch (Exception error) {
          throw new Exception("[UserService.updateUser] -> " + error.getMessage());
        }
    }
    

}
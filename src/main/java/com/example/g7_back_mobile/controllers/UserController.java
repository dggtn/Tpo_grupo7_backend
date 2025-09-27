package com.example.g7_back_mobile.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.g7_back_mobile.controllers.dtos.ResponseData;
import com.example.g7_back_mobile.controllers.dtos.UserDTO;
import com.example.g7_back_mobile.repositories.entities.User;
import com.example.g7_back_mobile.services.UserService;
import com.example.g7_back_mobile.services.exceptions.UserException;

import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PutMapping("/name")
    public ResponseEntity<ResponseData<?>> actulizarNombre(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserDTO userDTO) {
        try {
            User authUser = userService.getUserByEmail(userDetails.getUsername());
            authUser.setFirstName(userDTO.getFirstName());
            User updatedUser = userService.updateUser(authUser);
            UserDTO updatedUserDTO = updatedUser.toDTO();
            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(updatedUserDTO));
        } catch (UserException error) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseData.error(error.getMessage()));
        } catch (Exception error) {
            System.out.printf("[UserController.updateUser] -> %s", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseData.error("No se pudo actualizar el usuario"));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseData<?>> updateUser(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserDTO userDTO) {
        try {
            // CAMBIO: Usar getUserByEmail en lugar de getUserByUsername
            User authUser = userService.getUserByEmail(userDetails.getUsername());

            User user = userDTO.toEntity();
            authUser.updateData(user);

            String password = user.getPassword();
            if(!password.equals("null")) authUser.setPassword(passwordEncoder.encode(password));

            User updatedUser = userService.updateUser(authUser);
            UserDTO updatedUserDTO = updatedUser.toDTO();

            return ResponseEntity.status(HttpStatus.OK).body(ResponseData.success(updatedUserDTO));

        } catch (UserException error) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseData.error(error.getMessage()));
        } catch (Exception error) {
            System.out.printf("[UserController.updateUser] -> %s", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseData.error("No se pudo actualizar el usuario"));
        }
    }
    @GetMapping
    public ResponseEntity<ResponseData<?>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<UserDTO> userDTOs = users.stream()
                    .map(User::toDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseData.success(userDTOs));
        } catch (Exception error) {
            System.out.printf("[UserController.getAllUsers] -> %s", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseData.error("No se pudieron obtener los usuarios"));
        }
    }
	
	@GetMapping("/me")
	public ResponseEntity<ResponseData<?>> me(@AuthenticationPrincipal UserDetails userDetails) {
    try {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseData.error("No autenticado"));
        }

        // IMPORTANTE: en tu app el subject del JWT es el email.
        User user = userService.getUserByEmail(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseData.error("Usuario no encontrado"));
        }

        UserDTO dto = user.toDTO();
        return ResponseEntity.ok(ResponseData.success(dto));
    } catch (Exception e) {
        System.out.printf("[UserController.me] -> %s%n", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("No se pudo obtener el usuario actual"));
    }
}


}

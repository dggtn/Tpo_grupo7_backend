package com.example.g7_back_mobile.controllers.dtos;

import com.example.g7_back_mobile.repositories.entities.Role;
import com.example.g7_back_mobile.repositories.entities.User;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;

    private String username;
 
    private String firstName; 
  
    private String lastName;
    @NotNull
    private String email;
   
    private String password;
    @NotNull
    private Role role;

    public User toEntity() {
        return new User(
                this.id,
                this.username,
                this.firstName,
                this.lastName,
                this.email,
                this.password,
                this.role
                );     
                
    }
   
    
}
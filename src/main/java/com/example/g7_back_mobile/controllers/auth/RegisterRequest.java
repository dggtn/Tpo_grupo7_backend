package com.example.g7_back_mobile.controllers.auth;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    private String username;
    private String firstName;
    private String lastName;
    @NotNull
    private String email;
    @NotNull
    private String password;
    private Integer age;
    private String address;
    protected String urlAvatar;
}

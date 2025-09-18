package com.example.g7_back_mobile.controllers.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerificarEmailRequest {
    @NotNull
    @Email
    private String email;
}
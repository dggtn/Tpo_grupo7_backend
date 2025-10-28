package com.example.g7_back_mobile.controllers.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyResetCodeRequest {
    @NotNull
    @Email
    private String email;
    
    @NotNull
    @Size(min = 4, max = 4)
    private String code;
}
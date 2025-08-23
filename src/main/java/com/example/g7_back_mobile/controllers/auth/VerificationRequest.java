package com.example.g7_back_mobile.controllers.auth;
import lombok.Data;

@Data
public class VerificationRequest {
    private String email;
    private String code;
}

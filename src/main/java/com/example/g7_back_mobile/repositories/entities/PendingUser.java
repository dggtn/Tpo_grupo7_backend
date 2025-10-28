package com.example.g7_back_mobile.repositories.entities;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pending_users")
public class PendingUser {
    @Id
    private String email;
    
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private Integer age;
    private String address;
    private String urlAvatar;
    
    private String verificationCode;
    private LocalDateTime expiryDate;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoReenvio;
    private Integer intentosReenvio;
    
    // Campo para diferenciar registro vs recuperación
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type")
    private OperationType operationType;
    
    // Enum para tipo de operación
    public enum OperationType {
        REGISTRATION,  // Registro de nuevo usuario
        PASSWORD_RESET // Recuperación de contraseña
    }
}
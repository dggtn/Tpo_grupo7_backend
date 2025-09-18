package com.example.g7_back_mobile.repositories.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PendingUser {
    @Id
    private String email;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private Integer age;
    private String address;
    protected String urlAvatar;

    // Campos para la verificación
    private String verificationCode;
    private LocalDateTime expiryDate;
    
    // NUEVOS CAMPOS PARA RECUPERACIÓN
    private Integer intentosReenvio; // Número de veces que se ha reenviado el código
    private LocalDateTime ultimoReenvio; // Última vez que se envió un código
    private LocalDateTime fechaCreacion; // Cuando se creó el registro pendiente por primera vez
}

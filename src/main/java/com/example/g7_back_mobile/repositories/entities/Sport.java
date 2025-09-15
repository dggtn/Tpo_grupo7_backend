package com.example.g7_back_mobile.repositories.entities;
import com.example.g7_back_mobile.controllers.dtos.SportDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Sport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sportTypeName;

    public SportDTO toEntity(){
        return new SportDTO(
            this.id,
            this.sportTypeName
           
        );
    }


}

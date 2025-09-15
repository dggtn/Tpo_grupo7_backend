package com.example.g7_back_mobile.controllers.dtos;

import com.example.g7_back_mobile.repositories.entities.Sport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SportDTO {
    private Long id;
    private String sportTypeName;

    public Sport toEntity(){
        return new Sport(
            this.id,
            this.sportTypeName
           
        );
    }
}

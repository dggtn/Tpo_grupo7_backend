package com.example.g7_back_mobile.controllers.dtos;

import com.example.g7_back_mobile.repositories.entities.Teacher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherDTO {
    private Long id;
    private String name;

     public Teacher toEntity(){
        return new Teacher(
            this.id,
            this.name
           
        );
    }
    
}

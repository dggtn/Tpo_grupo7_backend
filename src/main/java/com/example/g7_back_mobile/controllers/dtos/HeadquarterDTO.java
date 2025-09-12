package com.example.g7_back_mobile.controllers.dtos;

import com.example.g7_back_mobile.repositories.entities.Headquarter;
import com.example.g7_back_mobile.repositories.entities.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeadquarterDTO {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private String email;
    private String whattsapp;
    private Location location;

    public Headquarter toEntity(){
        return new Headquarter(
            this.id,
            this.name,
            this.phone,
            this.address,
            this.email,
            this.whattsapp,
            this.location
           
        );
    }
}
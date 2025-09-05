package com.example.g7_back_mobile.repositories.entities;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.g7_back_mobile.controllers.dtos.UserDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class User implements UserDetails{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String username;
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column(unique = true)
    private String email;
    @Column
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column
    private Integer age;
    @Column
    private String address;
    @Column(columnDefinition = "LONGTEXT")
    protected String urlAvatar;

    public UserDTO toDTO() {
        return new UserDTO(
            this.id,
            this.username,
            this.firstName,
            this.lastName,
            this.email,
            this.password,
            this.role,
            this.age,
            this.address,
            this.urlAvatar
            );
    }


    public void updateData(User newUser){
        setFirstName(newUser.getFirstName());
        setLastName(newUser.getLastName());
        setEmail(newUser.getEmail());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    
}

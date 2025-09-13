package com.example.g7_back_mobile.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.g7_back_mobile.repositories.SportRepository;
import com.example.g7_back_mobile.repositories.entities.Sport;

@Service
public class SportService {

    @Autowired
    private SportRepository sportRepository;

    public List<Sport> getAllSports() throws Exception {
      try {
        List<Sport> sports = sportRepository.findAll();
        return sports;
      } catch (Exception error) {
        throw new Exception("[SportService.getAllSports] -> " + error.getMessage());
      }
    }

    public Sport createSport(Sport sport) throws Exception {
      try {
        Sport createdSport = sportRepository.save(sport);
        return createdSport;
      } catch (Exception error) {
        throw new Exception("[SportService.createSport] -> " + error.getMessage());
      }
    }

    
}

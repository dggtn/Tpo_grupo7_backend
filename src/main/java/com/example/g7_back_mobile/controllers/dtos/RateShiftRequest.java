package com.example.g7_back_mobile.controllers.dtos;

import lombok.Data;

@Data
public class RateShiftRequest {
    private int rating;
    private String comment;
}

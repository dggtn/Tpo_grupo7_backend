package com.example.g7_back_mobile.controllers.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RateRequest {
    private String userEmail;
    private Long ShiftId;
    private int rating;
    private String comment;
}

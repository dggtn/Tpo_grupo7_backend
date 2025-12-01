package com.example.g7_back_mobile.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private String type;
    private String message;
    private String title;
    private Long classId;
    private Long shiftId;
    private Long reservationId;
    private String classStartAt; // ISO format
    private String sede;
    private String courseName;
    private String timestamp;
}

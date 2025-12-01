package com.example.g7_back_mobile.controllers.dtos;

import com.example.g7_back_mobile.repositories.entities.UserEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEventDTO {
    
    private Long id;
    private String eventType;
    private String title;
    private String message;
    private Long relatedShiftId;
    private Long relatedCourseId;
    private LocalDateTime scheduledTime;
    private LocalDateTime createdAt;
    private Boolean read;
    private String metadata;
    
    public static UserEventDTO fromEntity(UserEvent event) {
        return UserEventDTO.builder()
            .id(event.getId())
            .eventType(event.getEventType().name())
            .title(event.getTitle())
            .message(event.getMessage())
            .relatedShiftId(event.getRelatedShiftId())
            .relatedCourseId(event.getRelatedCourseId())
            .scheduledTime(event.getScheduledTime())
            .createdAt(event.getCreatedAt())
            .read(event.getRead())
            .metadata(event.getMetadata())
            .build();
    }
}

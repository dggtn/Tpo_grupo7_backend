package com.example.g7_back_mobile.controllers;


import com.example.g7_back_mobile.controllers.dtos.EventDTO;
import com.example.g7_back_mobile.repositories.entities.UserEvent;
import com.example.g7_back_mobile.services.UserEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class LongpollController {
    
    private final UserEventService eventService;
    private static final ScheduledExecutorService scheduler = 
        Executors.newScheduledThreadPool(10);
    
    /**
     * Endpoint de long polling para obtener eventos
     * 
     * @param since Timestamp desde el cual buscar eventos (ISO format)
     * @param timeout Timeout en segundos (máximo 30s)
     * @param userDetails Usuario autenticado
     */
    @GetMapping("/poll")
    public DeferredResult<ResponseEntity<List<EventDTO>>> pollEvents(
            @RequestParam(required = false) String since,
            @RequestParam(defaultValue = "25") int timeout,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Validar timeout (máximo 30 segundos para evitar problemas)
        timeout = Math.min(timeout, 30);
        
        // Crear DeferredResult con timeout
        DeferredResult<ResponseEntity<List<EventDTO>>> deferredResult = 
            new DeferredResult<>((long) timeout * 1000);
        
        // Manejar timeout
        deferredResult.onTimeout(() -> {
            log.debug("[LongPoll] Timeout alcanzado para user={}", userDetails.getUsername());
            deferredResult.setResult(ResponseEntity.ok(List.of()));
        });
        
        // Obtener userId del usuario autenticado
        Long userId = extractUserId(userDetails);
        
        // Parsear timestamp
        LocalDateTime sinceDateTime;
        try {
            sinceDateTime = since != null && !since.isEmpty()
                ? LocalDateTime.parse(since, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : LocalDateTime.now().minusMinutes(5);
        } catch (Exception e) {
            log.warn("[LongPoll] Error parseando timestamp '{}': {}", since, e.getMessage());
            sinceDateTime = LocalDateTime.now().minusMinutes(5);
        }
        
        // Polling con verificaciones periódicas cada 2 segundos
        LocalDateTime finalSinceDateTime = sinceDateTime;
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (deferredResult.isSetOrExpired()) {
                    return; // Ya se completó o expiró
                }
                
                List<UserEvent> events = eventService.getEventsSince(userId, finalSinceDateTime);
                
                if (!events.isEmpty()) {
                    // Convertir a DTOs
                    List<EventDTO> eventDTOs = events.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
                    
                    // Marcar como consumidos
                    List<Long> eventIds = events.stream()
                        .map(UserEvent::getId)
                        .collect(Collectors.toList());
                    eventService.markEventsAsConsumed(eventIds);
                    
                    log.info("[LongPoll] Retornando {} eventos para user={}", 
                        eventDTOs.size(), userDetails.getUsername());
                    
                    deferredResult.setResult(ResponseEntity.ok(eventDTOs));
                }
                
            } catch (Exception e) {
                log.error("[LongPoll] Error en polling: {}", e.getMessage());
                deferredResult.setErrorResult(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(List.of())
                );
            }
        }, 0, 2, TimeUnit.SECONDS);
        
        return deferredResult;
    }
    
    /**
     * Endpoint simple para verificar eventos pendientes (sin long polling)
     */
    @GetMapping("/check")
    public ResponseEntity<List<EventDTO>> checkEvents(
            @RequestParam(required = false) String since,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = extractUserId(userDetails);
        
        LocalDateTime sinceDateTime;
        try {
            sinceDateTime = since != null && !since.isEmpty()
                ? LocalDateTime.parse(since, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : LocalDateTime.now().minusHours(1);
        } catch (Exception e) {
            sinceDateTime = LocalDateTime.now().minusHours(1);
        }
        
        List<UserEvent> events = eventService.getEventsSince(userId, sinceDateTime);
        
        List<EventDTO> eventDTOs = events.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        if (!events.isEmpty()) {
            List<Long> eventIds = events.stream()
                .map(UserEvent::getId)
                .collect(Collectors.toList());
            eventService.markEventsAsConsumed(eventIds);
        }
        
        return ResponseEntity.ok(eventDTOs);
    }
    
    /**
     * Convertir UserEvent a DTO
     */
    private EventDTO convertToDTO(UserEvent event) {
        return EventDTO.builder()
            .type(event.getType().name())
            .message(event.getMessage())
            .title(event.getTitle())
            .classId(event.getClassId())
            .shiftId(event.getShiftId())
            .reservationId(event.getReservationId())
            .classStartAt(event.getClassStartAt() != null 
                ? event.getClassStartAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null)
            .sede(event.getSede())
            .courseName(event.getCourseName())
            .timestamp(event.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .build();
    }
    
    /**
     * Extraer userId del UserDetails
     * NOTA: Ajustar según tu implementación de UserDetails
     */
    private Long extractUserId(UserDetails userDetails) {
        // Opción 1: Si UserDetails es tu entidad User
        if (userDetails instanceof com.example.g7_back_mobile.repositories.entities.User) {
            return ((com.example.g7_back_mobile.repositories.entities.User) userDetails).getId();
        }
        
        // Opción 2: Parsear del username si guardas el ID allí
        try {
            return Long.parseLong(userDetails.getUsername());
        } catch (NumberFormatException e) {
            // Opción 3: Buscar por username/email
            log.warn("[LongPoll] No se pudo extraer userId de UserDetails");
            return 1L; // Valor por defecto - AJUSTAR SEGÚN TU LÓGICA
        }
    }
}


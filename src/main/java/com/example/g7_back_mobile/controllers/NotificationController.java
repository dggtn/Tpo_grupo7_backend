package com.example.g7_back_mobile.controllers;

import com.example.g7_back_mobile.controllers.dtos.ResponseData;
import com.example.g7_back_mobile.controllers.dtos.UserEventDTO;
import com.example.g7_back_mobile.repositories.entities.User;
import com.example.g7_back_mobile.services.UserEventService;
import com.example.g7_back_mobile.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private UserEventService userEventService;
    
    @Autowired
    private UserService userService;

    /**
     * Endpoint de long polling para obtener eventos pendientes
     * El frontend puede llamar a este endpoint periódicamente (ej: cada 30-60 segundos)
     * 
     * GET /notifications/poll
     */
    @GetMapping("/poll")
    public ResponseEntity<ResponseData<?>> pollNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // ✅ VALIDACIÓN ADICIONAL
            if (userDetails == null) {
                System.err.println("[NotificationController.pollNotifications] UserDetails es null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseData.error("No autenticado"));
            }
            
            System.out.println("[NotificationController.pollNotifications] Polling para usuario: " 
                + userDetails.getUsername());
            
            User user = userService.getUserByEmail(userDetails.getUsername());
            
            if (user == null || user.getId() == null) {
                System.err.println("[NotificationController.pollNotifications] Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseData.error("Usuario no encontrado"));
            }
            
            List<UserEventDTO> events = userEventService.getPendingEvents(user.getId());
            
            // ✅ SIEMPRE DEVOLVER RESPUESTA EXITOSA (aunque sea lista vacía)
            System.out.println("[NotificationController.pollNotifications] Devolviendo " 
                + events.size() + " eventos");
            
            return ResponseEntity.ok(ResponseData.success(events));
            
        } catch (Exception e) {
            System.err.println("[NotificationController.pollNotifications] Error: " + e.getMessage());
            e.printStackTrace();
            
            // ✅ DEVOLVER LISTA VACÍA EN LUGAR DE ERROR para no romper el polling
            return ResponseEntity.ok(ResponseData.success(new java.util.ArrayList<>()));
        }
    }

    /**
     * Marcar notificaciones como leídas
     * POST /notifications/mark-read
     * Body: { "eventIds": [1, 2, 3] }
     */
    @PostMapping("/mark-read")
    public ResponseEntity<ResponseData<?>> markAsRead(
            @RequestBody MarkReadRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (request.getEventIds() == null || request.getEventIds().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ResponseData.error("eventIds es requerido"));
            }
            
            System.out.println("[NotificationController.markAsRead] Marcando " 
                + request.getEventIds().size() + " eventos como leídos");
            
            userEventService.markAsRead(request.getEventIds());
            
            return ResponseEntity.ok(ResponseData.success("Notificaciones marcadas como leídas"));
            
        } catch (Exception e) {
            System.err.println("[NotificationController.markAsRead] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("Error marcando notificaciones"));
        }
    }

    /**
     * Obtener contador de notificaciones no leídas
     * GET /notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ResponseData<?>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseData.error("No autenticado"));
            }
            
            User user = userService.getUserByEmail(userDetails.getUsername());
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseData.error("Usuario no encontrado"));
            }
            
            long count = userEventService.countUnreadEvents(user.getId());
            
            System.out.println("[NotificationController.getUnreadCount] Usuario " 
                + user.getId() + " tiene " + count + " eventos no leídos");
            
            return ResponseEntity.ok(ResponseData.success(count));
            
        } catch (Exception e) {
            System.err.println("[NotificationController.getUnreadCount] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ResponseData.success(0L));
        }
    }

    // DTO interno para el request
    public static class MarkReadRequest {
        private List<Long> eventIds;

        public List<Long> getEventIds() {
            return eventIds;
        }

        public void setEventIds(List<Long> eventIds) {
            this.eventIds = eventIds;
        }
    }
}
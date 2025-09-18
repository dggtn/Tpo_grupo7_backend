package com.example.g7_back_mobile.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.g7_back_mobile.repositories.PendingUserRepository;
import com.example.g7_back_mobile.repositories.entities.PendingUser;

@Service
public class CleanupService {
    
    @Autowired
    private PendingUserRepository pendingUserRepository;
    
    // Ejecutar cada hora para limpiar registros expirados completamente
    @Scheduled(fixedRate = 3600000) // 1 hora
    public void limpiarRegistrosExpirados() {
        try {
            LocalDateTime limiteExpiracion = LocalDateTime.now().minusHours(24);
            
            List<PendingUser> registrosExpirados = pendingUserRepository.findAll()
                .stream()
                .filter(user -> user.getFechaCreacion() != null && 
                               user.getFechaCreacion().isBefore(limiteExpiracion))
                .collect(Collectors.toList());
            
            if (!registrosExpirados.isEmpty()) {
                pendingUserRepository.deleteAll(registrosExpirados);
                System.out.println("[CleanupService] Eliminados " + registrosExpirados.size() + 
                    " registros pendientes expirados");
            }
            
        } catch (Exception e) {
            System.err.println("[CleanupService] Error en limpieza: " + e.getMessage());
        }
    }
}

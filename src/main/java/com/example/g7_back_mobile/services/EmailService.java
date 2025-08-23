package com.example.g7_back_mobile.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Tomamos el email desde application.properties para no escribirlo aquí
    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Tu Código de Verificación para la App de RitmoFit");
        message.setText("Hola,\n\nGracias por registrarte. Tu código de verificación es: " + code + "\n\nEste código expirará en 15 minutos.\n\nSaludos,\nEl equipo de RitmoFit");

        mailSender.send(message);
        System.out.println("Correo de verificación enviado a: " + toEmail);
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        System.out.println("Correo de confirmación de curso enviado a: " + to);
    }
}
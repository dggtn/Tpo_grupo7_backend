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

    // Método para reenvío de codigo
    public void sendVerificationCodeResend(String toEmail, String code, int intentoNumero) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Código de Verificación Reenviado - RitmoFit");
        
        String body = String.format(
            "Hola,\n\n" +
            "Has solicitado un nuevo código de verificación para completar tu registro.\n\n" +
            "Tu nuevo código de verificación es: %s\n\n" +
            "Este código expirará en 15 minutos.\n" +
            "Este es tu intento #%d de reenvío.\n\n" +
            "Si no solicitaste este código, puedes ignorar este mensaje.\n\n" +
            "Saludos,\n" +
            "El equipo de RitmoFit",
            code, intentoNumero
        );
        
        message.setText(body);
        mailSender.send(message);
        System.out.println("Código de verificación reenviado a: " + toEmail + " (Intento #" + intentoNumero + ")");
    }

    //---Email inicial de recuperación de contraseña----
   
    public void sendPasswordResetCode(String toEmail, String code, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Código de Recuperación de Contraseña - RitmoFit");
        
        String greeting = firstName != null ? "Hola " + firstName + "," : "Hola,";
        
        String body = String.format(
            "%s\n\n" +
            "Hemos recibido una solicitud para restablecer tu contraseña.\n\n" +
            "Tu código de verificación es: %s\n\n" +
            "Este código expirará en 15 minutos.\n\n" +
            "Si no solicitaste este cambio, puedes ignorar este mensaje.\n" +
            "Tu contraseña no se modificará a menos que ingreses este código en la aplicación.\n\n" +
            "Por seguridad:\n" +
            "- No compartas este código con nadie\n" +
            "- RitmoFit nunca te pedirá este código por teléfono o email\n\n" +
            "Saludos,\n" +
            "El equipo de RitmoFit",
            greeting, code
        );
        
        message.setText(body);
        mailSender.send(message);
        System.out.println("Código de recuperación enviado a: " + toEmail);
    }

    //----Email de reenvío de código de recuperación de contraseña----
  
    public void sendPasswordResetCodeResend(String toEmail, String code, int intentoNumero, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Código de Recuperación Reenviado - RitmoFit");
        
        String greeting = firstName != null ? "Hola " + firstName + "," : "Hola,";
        
        String body = String.format(
            "%s\n\n" +
            "Has solicitado un nuevo código para restablecer tu contraseña.\n\n" +
            "Tu nuevo código de verificación es: %s\n\n" +
            "Este código expirará en 15 minutos.\n" +
            "Este es tu intento #%d de reenvío.\n\n" +
            "Si no solicitaste este código, ignora este mensaje.\n\n" +
            "Saludos,\n" +
            "El equipo de RitmoFit",
            greeting, code, intentoNumero
        );
        
        message.setText(body);
        mailSender.send(message);
        System.out.println("Código de recuperación reenviado a: " + toEmail + " (Intento #" + intentoNumero + ")");
    }

    //----Email de confirmación de cambio de contraseña---
  
    public void sendPasswordResetConfirmation(String toEmail, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Contraseña Actualizada Exitosamente - RitmoFit");
        
        String greeting = firstName != null ? "Hola " + firstName + "," : "Hola,";
        
        String body = String.format(
            "%s\n\n" +
            "Te confirmamos que tu contraseña ha sido actualizada exitosamente.\n\n" +
            "Si no realizaste este cambio, por favor contacta inmediatamente a nuestro equipo de soporte.\n\n" +
            "Por tu seguridad, te recomendamos:\n" +
            "- Usar una contraseña única y segura\n" +
            "- No compartir tu contraseña con nadie\n" +
            "- Cambiar tu contraseña periódicamente\n\n" +
            "Saludos,\n" +
            "El equipo de RitmoFit",
            greeting
        );
        
        message.setText(body);
        mailSender.send(message);
        System.out.println("Confirmación de cambio de contraseña enviada a: " + toEmail);
    }

}
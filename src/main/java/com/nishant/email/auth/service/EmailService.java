package com.nishant.email.auth.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    /*public void sendWelcomeEmail(String toEmail, String name){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Welcome to Our Service");
        message.setText("Hello " + name + ",\n\nWelcome to our service! Thanks for registering with us.\n\nBest regards,\nThe Authentication Team");
        javaMailSender.send(message);
    }*/

    /**
     * Send Welcome Email After Successful Registration
     */
    public void sendWelcomeEmail(String toEmail, String name) {
        sendHtmlEmail(toEmail, "Welcome to Authify 🎉",
                loadTemplate("templates/welcome-email.html").replace("{{NAME}}", name));
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Authify Support");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String loadTemplate(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email template", e);
        }
    }

    /*public void sendResetOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset OTP");
        message.setText("Your OTP for password reset is: " + otp + "\n\nThis OTP is valid for 5 minutes.");
        javaMailSender.send(message);
    }
*/
    public void sendResetOtpEmail(String toEmail, String otp) {
        try {
            String htmlTemplate = loadTemplate("templates/password-reset-otp.html");
            String finalHtml = htmlTemplate.replace("{{OTP}}", otp);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Authify Support");
            helper.setTo(toEmail);
            helper.setSubject("Your OTP for Password Reset");
            helper.setText(finalHtml, true);

            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send reset OTP email", e);
        }
    }

    public void sendOtpEmail(String toEmail, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("OTP for Authentication");
        message.setText("Your OTP for authentication is: " + otp + "\n\nThis OTP is valid for 24 hours.");
        javaMailSender.send(message);
    }
}
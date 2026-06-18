package com.fooddelivery.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String content) {
        System.out.println("\n=================================================");
        System.out.println("📧 MOCK EMAIL DISPATCH LOG");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Content:\n" + content);
        System.out.println("=================================================\n");

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject(subject);
                message.setText(content);
                mailSender.send(message);
                System.out.println("✅ Real email successfully sent via SMTP to: " + to);
            } catch (Exception e) {
                System.err.println("❌ Failed to send real email via SMTP: " + e.getMessage());
            }
        }
    }
}

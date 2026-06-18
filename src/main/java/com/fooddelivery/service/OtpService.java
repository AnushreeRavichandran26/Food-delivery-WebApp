package com.fooddelivery.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private EmailService emailService;

    // Stores OTP details: key = email, value = OtpDetails
    private final Map<String, OtpDetails> otpStorage = new ConcurrentHashMap<>();
    
    // Stores verified emails: key = email, value = expiration time
    private final Map<String, LocalDateTime> verifiedEmails = new ConcurrentHashMap<>();

    private static class OtpDetails {
        String code;
        LocalDateTime expiryTime;

        OtpDetails(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }

    public void sendOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5); // 5 minutes validity
        otpStorage.put(email.toLowerCase().trim(), new OtpDetails(otp, expiry));

        String subject = "Your ADS Verification Code";
        String content = "Hello,\n\nYour 6-digit OTP code to verify your Food Delivery account is:\n\n"
                + otp + "\n\nThis code is valid for 5 minutes.\n\nThank you,\nFood Delivery Team";

        emailService.sendEmail(email, subject, content);
    }

    public boolean verifyOtp(String email, String otp) {
        String cleanEmail = email.toLowerCase().trim();
        OtpDetails details = otpStorage.get(cleanEmail);
        
        if (details == null) {
            return false;
        }
        
        if (details.expiryTime.isBefore(LocalDateTime.now())) {
            otpStorage.remove(cleanEmail);
            return false;
        }
        
        if (details.code.equals(otp.trim())) {
            otpStorage.remove(cleanEmail);
            verifiedEmails.put(cleanEmail, LocalDateTime.now().plusMinutes(15)); // Valid for registration for 15 mins
            return true;
        }
        
        return false;
    }

    public boolean isEmailVerified(String email) {
        String cleanEmail = email.toLowerCase().trim();
        LocalDateTime expiry = verifiedEmails.get(cleanEmail);
        if (expiry == null) {
            return false;
        }
        if (expiry.isBefore(LocalDateTime.now())) {
            verifiedEmails.remove(cleanEmail);
            return false;
        }
        return true;
    }

    public void consumeEmailVerification(String email) {
        verifiedEmails.remove(email.toLowerCase().trim());
    }
}

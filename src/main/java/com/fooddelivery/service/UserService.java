package com.fooddelivery.service;

import com.fooddelivery.model.User;
import com.fooddelivery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @jakarta.annotation.PostConstruct
    public void seedAdminUser() {
        if (!userRepository.existsByEmail("admin@fooddelivery.com")) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@fooddelivery.com");
            admin.setPhone("1234567890");
            admin.setPassword(passwordEncoder.encode("AdminPassword123!"));
            admin.setRole("ADMIN");
            admin.setAddress("Admin HQ");
            admin.setCity("Admin City");
            admin.setPostalCode("12345");
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
        }
    }

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Skip OTP for Google-linked registrations
        if (!user.isGoogleLinked()) {
            if (!otpService.isEmailVerified(user.getEmail())) {
                throw new RuntimeException("Email verification via OTP is required before registration");
            }
            otpService.consumeEmailVerification(user.getEmail());
        }

        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("CUSTOMER");
        }
        // Only encode password if one was supplied
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(""); // Google users have no password
        }
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Google Sign-In: find-or-create a user by email.
     * Trust is established by the Google JWT token verified client-side.
     * No password is checked or stored for Google-linked accounts.
     */
    public User googleLoginOrRegister(String email, String name, String googleName) {
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            User user = existing.get();
            // Link to Google if not already linked
            if (!user.isGoogleLinked()) {
                user.setGoogleLinked(true);
                user.setGoogleName(googleName);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
            return user;
        }

        // New Google user — auto-register
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setGoogleLinked(true);
        newUser.setGoogleName(googleName);
        newUser.setPhone("");
        newUser.setPassword(""); // No password for Google-only accounts
        newUser.setRole("CUSTOMER");
        newUser.setAddress("");
        newUser.setCity("");
        newUser.setCreatedAt(LocalDateTime.now());
        return userRepository.save(newUser);
    }

    public Optional<User> loginUser(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            return user;
        }
        return Optional.empty();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        user.setName(userDetails.getName());
        user.setPhone(userDetails.getPhone());
        user.setAddress(userDetails.getAddress());
        user.setCity(userDetails.getCity());
        user.setPostalCode(userDetails.getPostalCode());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}

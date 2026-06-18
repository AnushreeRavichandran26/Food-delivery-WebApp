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
        
        // Google authentication simulation bypass check
        boolean isGoogleSim = "GoogleAuthSimulatedPassword123!".equals(user.getPassword());
        if (!isGoogleSim) {
            if (!otpService.isEmailVerified(user.getEmail())) {
                throw new RuntimeException("Email verification via OTP is required before registration");
            }
            otpService.consumeEmailVerification(user.getEmail());
        }

        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("CUSTOMER");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
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

package com.example.demo.Service;

import com.example.demo.Models.Doctor;
import com.example.demo.Models.Patient;
import com.example.demo.Models.User;
import com.example.demo.respository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import com.example.demo.Models.Admin;


@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Injected from PassConfig

    // Constructor injection (NO @Autowired needed)
    public UserService(UserRepository userRepository, 
                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    /**
     * Updates the last login timestamp for a user
     * @param email The email of the user to update
     */
    public void updateLastLogin(String email) {
        User user = findByEmail(email);
        user.recordLogin();
        userRepository.save(user);
    }

    /**
     * Checks if an email already exists in the system
     * @param email The email to check
     * @return true if the email exists, false otherwise
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Activates a user account
     * @param user The user to activate
     * @return The activated user
     */
    public User enableUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.isActive()) {
            throw new IllegalStateException("User is already active");
        }
        user.reactivate();
        return userRepository.save(user);
    }

    /**
     * Registers a new user with an encoded password
     * @param user The user to register
     * @param rawPassword The plaintext password to encode and store
     * @return The registered user
     */
    public User registerUser(User user, String rawPassword) {
        if (emailExists(user.getEmail())) {
            throw new IllegalStateException("Email already registered");
        }
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public Optional<User> findByuserId(String userId){
        return userRepository.findByUserId(userId);
    }

    /**
     * Changes a user's password
     * @param email The user's email
     * @param newPassword The new plaintext password to encode and store
     */
    public void changePassword(String email, String newPassword) {
        User user = findByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Deactivates a user account
     * @param user The user to deactivate
     * @return The deactivated user
     */
    public User disableUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (!user.isActive()) {
            throw new IllegalStateException("User is already inactive");
        }
        user.deactivate();
        return userRepository.save(user);
    }

    /**
     * Finds a user by email
     * @param email The email to search for
     * @return The found user
     * @throws IllegalArgumentException if user not found
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    /**
     * Verifies if the provided raw password matches the stored encoded password
     * @param user The user to verify
     * @param rawPassword The plaintext password to verify
     * @return true if passwords match, false otherwise
     */
    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }



      public Optional<Admin> findAdminForUser(User user) {
        if (user instanceof Doctor) {
            return userRepository.findAdminByManagedDoctor( user.getUserId());
        } else if (user instanceof Patient) {
            return userRepository.findAdminByManagedPatient(user.getUserId());
        }
        return Optional.empty();
    }



 public User findByUserId(String userId){
return userRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException("No user with this ID"));

}}

package com.example.QuizQuestArena_Backend.controller;

import com.example.QuizQuestArena_Backend.db.PasswordResetRepo;
import com.example.QuizQuestArena_Backend.model.PasswordReset;
import com.example.QuizQuestArena_Backend.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordResetRepo passwordResetRepo;

    @GetMapping("/password-reset")
    public ResponseEntity<Map<String, String>> showPasswordResetPage() {
        Map<String, String> response = new HashMap<>();
        // Reset messages before rendering the page
        response.put("message", "Reset password endpoint available.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        Map<String, String> response = new HashMap<>();
        try {
            //handle password reset logic
            String result = passwordResetService.requestPasswordReset(email);
            response.put("successMessage", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("errorMessage", "Failed to send password reset email. Please try again.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    //show password reset page after user click the link in the email
    @GetMapping("/reset-password")
    public ResponseEntity<Map<String, String>> showResetPasswordForm(@RequestParam("token") String token) {
        Map<String, String> response = new HashMap<>();
        // Validate the token
        if (passwordResetService.isTokenValid(token)) {
            response.put("token", token);
            response.put("message", "Token is valid. Proceed with resetting your password.");
            return ResponseEntity.ok(response);
        } else {
            response.put("errorMessage", "Invalid or expired token.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    //Service Method to Validate Token
    public boolean isTokenValid(String token) {
        Optional<PasswordReset> resetRequestOptional = passwordResetRepo.findByToken(token);
        if (resetRequestOptional.isPresent()) {
            System.out.println("Token: " + token);
            System.out.println("Token valid: " + passwordResetService.isTokenValid(token));
            PasswordReset resetRequest = resetRequestOptional.get();
            return resetRequest.getExpiryDate().isAfter(LocalDateTime.now());
        }
        return false;
    }

    //Handle password reset form click submission
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> requestBody) {
        //accepting frontend to send new passwords and token as a RequestBody, rather than a Query Parameter
        String token = requestBody.get("token");
        String password = requestBody.get("password");
        String confirmPassword = requestBody.get("confirmPassword");

        Map<String, String> response = new HashMap<>();
        if (!password.equals(confirmPassword)) {
            System.out.println("Token: " + token);
            System.out.println("New Password: " + password);
            System.out.println("Confirm Password: " + confirmPassword);

            response.put("errorMessage", "Passwords do not match.");
            return ResponseEntity.badRequest().body(response);
        }

        String result = passwordResetService.resetPassword(token, password);
        if (result.equals("Password has been successfully reset.")) {
            System.out.println("Token: " + token);
            System.out.println("New Password: " + password);
            System.out.println("Confirm Password: " + confirmPassword);

            response.put("successMessage", result);
            return ResponseEntity.ok(response);
        } else {
            System.out.println("Token: " + token);
            System.out.println("New Password: " + password);
            System.out.println("Confirm Password: " + confirmPassword);
            response.put("errorMessage", result);
            return ResponseEntity.badRequest().body(response);
        }

    }

}

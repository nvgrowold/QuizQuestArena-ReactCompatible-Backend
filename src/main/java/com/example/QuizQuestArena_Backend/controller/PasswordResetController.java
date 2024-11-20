package com.example.QuizQuestArena_Backend.controller;

import com.example.QuizQuestArena_Backend.db.PasswordResetRepo;
import com.example.QuizQuestArena_Backend.model.PasswordReset;
import com.example.QuizQuestArena_Backend.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/password")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordResetRepo passwordResetRepo;

    @GetMapping("/password-reset")
    public String showPasswordResetPage(Model model) {
        // Reset messages before rendering the page
        model.addAttribute("successMessage", null);
        model.addAttribute("errorMessage", null);
        return "passwordReset"; // matches thymeleaf template html name
    }

    @PostMapping("/request-password-reset")
    public String requestPasswordReset(@RequestParam("email") String email, Model model) {
        try {
            //handle password reset logic
            String response = passwordResetService.requestPasswordReset(email);
            model.addAttribute("successMessage", response);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to send password reset email. Please try again.");
        }
        return "passwordReset"; // Return same template with messages
    }

    //show password reset page after user click the link in the email
    @GetMapping("/reset")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        // Validate the token
        String message;
        if (passwordResetService.isTokenValid(token)) {
            model.addAttribute("token", token);
            return "resetPasswordForm"; // Replace with the Thymeleaf template for password reset
        } else {
            model.addAttribute("errorMessage", "Invalid or expired token.");
            return "passwordResetError";
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
    public String resetPassword(@RequestParam("token") String token,
                                @RequestParam("password") String password,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            model.addAttribute("token", token);
            System.out.println("Token: " + token);
            System.out.println("New Password: " + password);
            System.out.println("Confirm Password: " + confirmPassword);

            return "resetPasswordForm";
        }

        String response = passwordResetService.resetPassword(token, password);
        if (response.equals("Password has been successfully reset.")) {
            System.out.println("Token: " + token);
            System.out.println("New Password: " + password);
            System.out.println("Confirm Password: " + confirmPassword);

            return "redirect:/login";
        } else {
            System.out.println("Token: " + token);
            System.out.println("New Password: " + password);
            System.out.println("Confirm Password: " + confirmPassword);
            model.addAttribute("errorMessage", response);
            model.addAttribute("token", token);
            return "resetPasswordForm";
        }

    }

}

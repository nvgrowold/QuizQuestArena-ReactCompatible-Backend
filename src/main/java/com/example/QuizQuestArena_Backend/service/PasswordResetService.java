package com.example.QuizQuestArena_Backend.service;

import com.example.QuizQuestArena_Backend.db.PasswordResetRepo;
import com.example.QuizQuestArena_Backend.db.UserRepo;
import com.example.QuizQuestArena_Backend.model.PasswordReset;
import com.example.QuizQuestArena_Backend.model.PlayerUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetRepo passwordResetRepo;

    @Autowired
    private UserRepo userRepo;

//    @Autowired
//    private JavaMailSender mailSender;

    @Autowired
    private EmailService emailService;

    /**
     * Handle password reset request.
     * emailing User's email.
     * returning Status message.
     */
    public String requestPasswordReset(String email) {
        // Validate if user exists
        Optional<PlayerUser> userOptional = userRepo.findByEmail(email);

        if (userOptional.isEmpty()) {
            return "No user found with the given email address.";
        }

        PlayerUser user = userOptional.get();

        // Check if there's an existing unexpired token for the email
        Optional<PasswordReset> existingToken = passwordResetRepo.findByEmail(email);
        if (existingToken.isPresent()) {
            PasswordReset resetRequest = existingToken.get();
            if (resetRequest.getExpiryDate().isAfter(LocalDateTime.now())) {
                // If the token is still valid, resend the email with the same token
                sendResetEmail(email, resetRequest.getToken());
                return "Password reset instructions have been resent to your email.";
            } else {
                // If the token has expired, delete it
                passwordResetRepo.delete(resetRequest);
            }
        }

        // Delete existing password reset tokens for this email to avoid duiplicated key generated
       // passwordResetRepo.deleteByEmail(email);

        // Generate a unique reset token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

        // Save the reset request
        PasswordReset resetRequest = new PasswordReset();
        resetRequest.setEmail(email);
        resetRequest.setToken(token);
        resetRequest.setExpiryDate(expiryDate);
        resetRequest.setUser(user);
        passwordResetRepo.save(resetRequest);//save before email sending

        // Send password reset email
        try {
            sendResetEmail(email,token);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to send password reset email. Please try again.";
        }

        return "Password reset instructions have been sent to your email.";
    }

    /*Send the password reset email.*/
    private void sendResetEmail(String email, String token) {
        String resetUrl = "http://localhost:3000/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String content =  "<html>" +
                "<body>" +
                "<p>Dear user,</p>" +
                "<p>You have requested to reset your password. Click the link below to reset your password:</p>" +
                "<p><a href=\"" + resetUrl + "\">Reset Password</a></p>" +
                "<p>If you didn't request this, you can ignore this email.</p>" +
                "<p>Note: This link will expire in 1 hour.</p>" +
                "</body>" +
                "</html>";

        // Use EmailService to send the email
        emailService.sendHtmlEmail(email, subject, content);
    }

    /**
     * Handle the actual password reset.
     * @param token Reset token.
     * @param newPassword New password.
     * @return Status message.
     */
    public String resetPassword(String token, String newPassword) {
        // Validate the token
        Optional<PasswordReset> resetRequestOptional = passwordResetRepo.findByToken(token);
        if (resetRequestOptional.isEmpty()) {
            return "Invalid token.";
        }

        PasswordReset resetRequest = resetRequestOptional.get();

        // Check if the token has expired
        if (resetRequest.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "Token has expired.";
        }
//*********never meet the password requiments************* remove it for now
        // Validate the new password
        if (!PasswordValidator.isValid(newPassword)) {
            System.out.println("New Password: " + newPassword);
            return "Password must be at least 6 characters long, contain at least one uppercase letter, one lowercase letter, and one number.";
        }

        // Update the user's password
        PlayerUser user = resetRequest.getUser();
        user.setPassword(newPassword); // Directly save the plain text password
        //user.setPassword(hashPassword(newPassword)); // Hash the password
        userRepo.save(user);

        // Remove the reset request after successful reset
        passwordResetRepo.delete(resetRequest);

        return "Password has been successfully reset.";
    }

    /**
     * Hash the password securely.
     * @param password Plain-text password.
     * @return Hashed password.
     */
    private String hashPassword(String password) {
        return org.springframework.security.crypto.bcrypt.BCrypt.hashpw(password, org.springframework.security.crypto.bcrypt.BCrypt.gensalt());
    }

    public boolean isTokenValid(String token) {
        Optional<PasswordReset> resetRequestOptional = passwordResetRepo.findByToken(token);
        if (resetRequestOptional.isPresent()) {
            PasswordReset resetRequest = resetRequestOptional.get();
            if (resetRequest.getExpiryDate().isBefore(LocalDateTime.now())) {
                passwordResetRepo.delete(resetRequest); // Remove expired token
                return false;
            }
            return true;
        }
        return false;
    }
}

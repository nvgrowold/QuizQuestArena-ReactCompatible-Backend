package com.example.QuizQuestArena_Backend.service;

import com.example.QuizQuestArena_Backend.db.UserRepo;
import com.example.QuizQuestArena_Backend.model.PlayerUser;
import com.example.QuizQuestArena_Backend.model.Quiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewQuizNotificationService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EmailService emailService; // Use EmailService for email sending

    /**
     * Sends a notification to all non-admin users about a new quiz.
     *
     * @param quiz The quiz object containing details about the new quiz.
     */
    public void sendQuizCreatedEmail(Quiz quiz) {
        // Fetch all users excluding admins
        List<PlayerUser> users = userRepo.findAllByRoleNot("ROLE_ADMIN");

        // Email content
        String subject = "New Quiz Tournament Created!";
        String plainTextMessage = String.format(
                "Hello,\n\nA new quiz tournament \"%s\" has been created. It will start on %s and end on %s.\n\nBest regards,\nQuizQuest Team",
                quiz.getName(), quiz.getStartDate(), quiz.getEndDate()
        );
        String htmlMessage = String.format(
                "<h1>New Quiz Tournament Created!</h1>" +
                        "<p>Hello,</p>" +
                        "<p>A new quiz tournament <strong>%s</strong> has been created. It will start on <strong>%s</strong> and end on <strong>%s</strong>.</p>" +
                        "<p>Best regards,<br>QuizQuest Team</p>",
                quiz.getName(), quiz.getStartDate(), quiz.getEndDate()
        );

        // Send email to each user
        for (PlayerUser user : users) {
            System.out.println("Sending notification for quiz: " + quiz.getId()+"To" + user.getEmail());
            emailService.sendHtmlEmail(user.getEmail(), subject, htmlMessage);
            // Optionally, use sendEmail() if plain text is needed
            // emailService.sendEmail(user.getEmail(), subject, plainTextMessage);
        }
    }
}

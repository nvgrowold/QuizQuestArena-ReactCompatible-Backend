package com.example.QuizQuestArena_Backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**To send both versions, email messages typically include:
 *A MIME multipart message with:
 *Plain-text content
 *HTML content
 *The recipient’s email client decides which version to display based on its capabilities and the user's settings.
*/
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    /**
     * Sends a plain-text email.
     *
     * @param to      Recipient email address.
     * @param subject Email subject.
     * @param body    Email body (plain text).
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body,  false); // Set `false` for plain-text content
            helper.setFrom("13911158281@163.com");

            mailSender.send(message);
            System.out.println("Email sent successfully to " + to);
        } catch (MessagingException e) {
            System.err.println("Error sending email to " + to);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Sends an HTML email.
     *
     * @param to          Recipient email address.
     * @param subject     Email subject.
     * @param htmlContent Email body (HTML content).
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // Enable HTML content
            helper.setFrom("13911158281@163.com");

            mailSender.send(message);
            System.out.println("HTML email sent successfully to " + to);
        } catch (MessagingException e) {
            System.err.println("Error sending HTML email to " + to);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}

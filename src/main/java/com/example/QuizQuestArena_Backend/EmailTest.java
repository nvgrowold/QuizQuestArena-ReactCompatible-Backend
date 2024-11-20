package com.example.QuizQuestArena_Backend;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.SimpleMailMessage;

import java.util.Properties;

public class EmailTest {
    public static void main(String[] args) {
        // Create a JavaMailSender instance
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.163.com");
        mailSender.setPort(465); // SSL port for 163.com
        mailSender.setUsername("13911158281@163.com"); // Your email address
        mailSender.setPassword("EYDPRAdE9iheqdkG"); // Your app-specific password or authorization code

        // Configure mail properties
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true"); // Enable SSL
        props.put("mail.smtp.starttls.enable", "false"); // Disable STARTTLS for SSL port
        props.put("mail.debug", "true"); // Enable debug logs for troubleshooting

        // Create a simple email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("13911158281@163.com"); // Must match your spring.mail.username
        message.setTo("limanwu16@gmail.com"); // Replace with the recipient's email
        message.setSubject("Test Email");
        message.setText("This is a test email sent from 163.com using Spring JavaMailSender.");

        // Send the email
        try {
            mailSender.send(message);
            System.out.println("Email sent successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending email.");
        }
    }
}


package com.example.QuizQuestArena_Backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode //for login function to compare and check if the object is already in the collection

@Entity
public class PlayerUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String address;

    private String role; // User role (e.g., ROLE_PLAYER)

    private int score;

    private String profilePicture; // Field for the file path or URL


    @Override
    public String toString() { //for login function, password excluded as sensitive information, will appear on log message
        return "PlayerUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", role='" + role + '\'' +
                ", score=" + score +
                ", profilePictureUrl='" + profilePicture + '\'' +
                '}';
    }
}
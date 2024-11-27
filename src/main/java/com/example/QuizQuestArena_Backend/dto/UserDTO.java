package com.example.QuizQuestArena_Backend.dto;

import com.example.QuizQuestArena_Backend.model.PlayerUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @ReadOnlyProperty
    private Long id; // Needed for identifying the user during updates

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;
    private String address;
    @ReadOnlyProperty
    private String role;

    // Use MultipartFile for file uploads
    private MultipartFile profilePicture;

    private List<Long> participatedQuizIds; // Only store quiz IDs

    // Overloaded constructor for Entity to DTO mapping
    public UserDTO(Long id, String username, String firstName, String lastName, String email,
                   String phoneNumber, String address, String role, String profilePictureUrl) {
        this.id = id;
        this.username = username;
        this.password = null; // Password hidden during mapping
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.profilePicture = null; // Profile picture not mapped here
    }

    // Constructor to map from PlayerUser entity to UserDTO
    public UserDTO(PlayerUser playerUser) {
        this.id = playerUser.getId();
        this.username = playerUser.getUsername();
        this.firstName = playerUser.getFirstName();
        this.lastName = playerUser.getLastName();
        this.email = playerUser.getEmail();
        this.phoneNumber = playerUser.getPhoneNumber();
        this.address = playerUser.getAddress();
        this.role = playerUser.getRole();
        this.participatedQuizIds = playerUser.getParticipatedQuizzes() != null
                ? playerUser.getParticipatedQuizzes().stream().map(quiz -> quiz.getId()).toList()
                : List.of(); // Handle null or empty lists
    }
}


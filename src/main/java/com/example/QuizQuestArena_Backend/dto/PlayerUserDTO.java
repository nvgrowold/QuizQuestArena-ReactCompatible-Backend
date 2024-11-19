package com.example.QuizQuestArena_Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerUserDTO {

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
    private String role;

    // Use MultipartFile for file uploads
    private MultipartFile profilePicture;

    // Overloaded constructor for Entity to DTO mapping
    public PlayerUserDTO(Long id, String username, String firstName, String lastName, String email,
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
}


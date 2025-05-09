package com.example.QuizQuestArena_Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

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

    private String role; // ROLE_PLAYER, ROLE_ADMIN

    private int score;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Score> scores; // Relationship to Score

    private String profilePicture; // file path or URL

    @ManyToMany
    @JoinTable(
            name = "quiz_participants",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "quiz_id")
    )
    @JsonIgnore // Prevent recursion during JSON serialization
    private List<Quiz> participatedQuizzes; // quizzes this user participated

    // Custom constructor matching the fields in mapToEntity
    public PlayerUser(Long id, String username, String password, String firstName, String lastName,
                      String email, String phoneNumber, String address, String role, int score, String profilePicture) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.score = score;
        this.profilePicture = profilePicture;
    }

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
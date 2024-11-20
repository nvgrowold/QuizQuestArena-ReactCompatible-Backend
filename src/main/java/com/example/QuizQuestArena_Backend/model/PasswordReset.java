package com.example.QuizQuestArena_Backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String token;

    private LocalDateTime expiryDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false) //if PasswordReset entity always be linked to a PlayerUser, consider adding nullable = false to the @JoinColumn
    private PlayerUser user;
}

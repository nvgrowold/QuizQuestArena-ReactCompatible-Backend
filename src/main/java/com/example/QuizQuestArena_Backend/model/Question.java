package com.example.QuizQuestArena_Backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text; // Question text
    private String type; // Question type: multiple or boolean

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz; // Associated quiz
}

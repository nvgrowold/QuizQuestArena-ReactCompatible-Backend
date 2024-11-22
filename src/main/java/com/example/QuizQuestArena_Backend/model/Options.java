package com.example.QuizQuestArena_Backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Options {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String optionText; // The text for the option

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;
}

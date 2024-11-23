package com.example.QuizQuestArena_Backend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizFeedback {
    private String question;
    private String playerAnswer;
    private String correctAnswer;
    private boolean correct;
}

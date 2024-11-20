package com.example.QuizQuestArena_Backend.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OpenTDBQuestion {
    private String question;
    private String type; // "multiple" or "boolean"
    private String difficulty;
    private String category;
    private String correct_answer;
    private List<String> incorrect_answers;
}


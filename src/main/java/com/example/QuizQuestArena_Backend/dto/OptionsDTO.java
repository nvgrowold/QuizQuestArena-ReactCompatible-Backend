package com.example.QuizQuestArena_Backend.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class OptionsDTO {
    private Long id;
    private String optionText;
    private boolean isCorrect;
}


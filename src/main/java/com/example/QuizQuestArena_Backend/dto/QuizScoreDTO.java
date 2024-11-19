package com.example.QuizQuestArena_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class QuizScoreDTO {
    private String quizName;
    private long totalPlayers;
    private double averageScore;
    private int likes;
    private String playerName;
    private LocalDateTime completedDate;
    private double playerScore;
}

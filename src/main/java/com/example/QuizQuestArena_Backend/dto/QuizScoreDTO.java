package com.example.QuizQuestArena_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class QuizScoreDTO {
    private Long quizId;
    private String quizName;
    private long totalPlayers;
    private double averageScore;
    private int likes;
    private Long playerId;
    private String playerName;
    private String completedDate;
    private double playerScore;

    //Contructor
    public QuizScoreDTO(Long quizId, String quizName, long totalPlayers, double averageScore, int likes,  Long playerId, String playerName, LocalDateTime completedDate, double playerScore) {
        this.quizId = quizId;
        this.quizName = quizName;
        this.totalPlayers = totalPlayers;
        this.averageScore = averageScore;
        this.likes = likes;
        this.playerId = playerId;
        this.playerName = playerName;
        this.completedDate = completedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.playerScore = playerScore;
    }
}

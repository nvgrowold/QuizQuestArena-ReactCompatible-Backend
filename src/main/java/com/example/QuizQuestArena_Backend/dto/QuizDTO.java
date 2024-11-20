package com.example.QuizQuestArena_Backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizDTO {
    private String name;
    private String category;
    private String difficulty;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

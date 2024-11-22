package com.example.QuizQuestArena_Backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizDTO {
    private Long id;

//    @NotBlank(message = "Quiz name is required.")
//    @Size(max = 100, message = "Quiz name must not exceed 100 characters.")
    private String name;

//    @NotBlank(message = "Category is required.")
//    @Size(max = 50, message = "Category must not exceed 50 characters.")
    private String category;

//    @NotBlank(message = "Difficulty is required.")
//    @Pattern(regexp = "Easy|Medium|Hard", message = "Difficulty must be Easy, Medium, or Hard.")
    private String difficulty;

//    @NotNull(message = "Start date is required.")
//    @FutureOrPresent(message = "Start date must be in the present or future.")
    private LocalDateTime startDate;

//    @NotNull(message = "End date is required.")
//    @Future(message = "End date must be in the future.")
    private LocalDateTime endDate;

    //for start quiz
    private List<QuestionDTO> questions;
}

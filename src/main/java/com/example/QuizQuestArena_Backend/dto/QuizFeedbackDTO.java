package com.example.QuizQuestArena_Backend.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizFeedbackDTO {
    private String questionText;      // The text of the question
    private String playerAnswer;      // The player's submitted answer
    private String correctAnswer;     // The correct answer for the question
    private boolean isCorrect;        // Whether the player's answer was correct

    @Override
    public String toString() {
        return "QuizFeedbackDTO{" +
                "questionText='" + questionText + '\'' +
                ", playerAnswer='" + playerAnswer + '\'' +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", isCorrect=" + isCorrect +
                '}';
    }
}

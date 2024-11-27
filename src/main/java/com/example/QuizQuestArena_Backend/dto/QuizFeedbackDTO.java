package com.example.QuizQuestArena_Backend.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuizFeedbackDTO {
    private String questionText;      // The text of the question
    private String playerAnswer;      // The player's submitted answer
    private String correctAnswer;     // The correct answer for the question
    private boolean isCorrect;        // Whether the player's answer was correct

    // Constructor with data sanitization
    public QuizFeedbackDTO(String questionText, String playerAnswer, String correctAnswer, boolean isCorrect) {
        this.questionText = sanitize(questionText);
        this.playerAnswer = sanitize(playerAnswer);
        this.correctAnswer = sanitize(correctAnswer);
        this.isCorrect = isCorrect;
    }
    // Helper method to sanitize input strings (trim and handle nulls)
    private String sanitize(String input) {
        return input != null ? input.trim() : "No Answer";
    }

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

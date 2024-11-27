package com.example.QuizQuestArena_Backend.dto;

import com.example.QuizQuestArena_Backend.model.Question;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Data Transfer Object for representing a Question.
 */
@Getter
@Setter
@NoArgsConstructor
public class QuestionDTO {
    private Long id;
    private String text;
    private Integer questionIndex;
    private List<OptionsDTO> options;
    private String correctAnswer;

    @Override
    public String toString() {
        return "QuestionDTO{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", questionIndex=" + questionIndex +
                ", options=" + options +
                ", correctAnswer='" + correctAnswer + '\''+
                '}';
    }

    public QuestionDTO(Long id, String text, Integer questionIndex, List<OptionsDTO> options, String correctAnswer) {
        this.id = id;
        this.text = text;
        this.questionIndex = questionIndex;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    /**
     * Constructor to map from a Question entity.
     */
    public QuestionDTO(Question question) {
        this.id = question.getId();
        this.text = question.getText();
        this.questionIndex = question.getQuestionIndex();
        this.correctAnswer = question.getCorrectAnswer();
        this.options = question.getOptions() != null
                ? question.getOptions().stream()
                .map(option -> new OptionsDTO(option.getId(), option.getOptionText(), option.isCorrect()))
                .collect(Collectors.toList())
                : List.of(); // Handle null options gracefully
    }
}

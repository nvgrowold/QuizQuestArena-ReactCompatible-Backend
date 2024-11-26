package com.example.QuizQuestArena_Backend.dto;

import com.example.QuizQuestArena_Backend.model.Options;
import com.example.QuizQuestArena_Backend.model.Question;
import lombok.*;

import java.util.List;


/**
 * Data Transfer Object for representing a Question.
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class QuestionDTO {
    private Long id;
    private String text;
    private Integer questionIndex;
    private List<String> options; // Ensure this matches the Question entity

    @Override
    public String toString() {
        return "QuestionDTO{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", options=" + options +
                '}';
    }

    public QuestionDTO(Long id, String text, List<String> options) {
        this.id = id;
        this.text = text;
        this.options = options; // Store the options as they are
    }
}

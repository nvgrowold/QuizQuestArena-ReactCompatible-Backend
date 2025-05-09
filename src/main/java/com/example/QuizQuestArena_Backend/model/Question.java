package com.example.QuizQuestArena_Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text; // Question text
    private String type; // Question type: multiple or boolean
    private String correctAnswer;
    private Integer questionIndex;
    private String playerAnswer;

    //question options
    @OneToMany(mappedBy = "question", fetch = FetchType.EAGER, cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonIgnore // Prevent circular reference during serialization
    private List<Options> options;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz; // Associated quiz
}

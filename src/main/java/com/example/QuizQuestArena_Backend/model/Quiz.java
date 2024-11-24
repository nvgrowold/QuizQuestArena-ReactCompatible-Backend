package com.example.QuizQuestArena_Backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String category;

    private String difficulty;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private int likes;

    //cascade remove parent and orphanRemoval will remove it's child as well
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Score> scores; // Relationship to Score

    @ManyToMany(mappedBy = "participatedQuizzes",fetch = FetchType.LAZY)
    private List<PlayerUser> participants = new ArrayList<>(); // Users who participated

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();; // Add this to map questions to the quiz

    //Avoid Replacing the Collection
    //Instead of replacing the questions list, update it in place. If you're setting a new list of questions, Hibernate treats the old list as orphaned and deletes it.
    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuiz(this); // Ensure the relationship is properly set
    }
    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setQuiz(null); // Break the relationship
    }

}

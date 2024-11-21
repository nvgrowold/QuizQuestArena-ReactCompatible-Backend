package com.example.QuizQuestArena_Backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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
}

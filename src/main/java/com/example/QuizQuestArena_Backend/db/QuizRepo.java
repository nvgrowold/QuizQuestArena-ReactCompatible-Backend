package com.example.QuizQuestArena_Backend.db;

import com.example.QuizQuestArena_Backend.dto.QuizScoreDTO;
import com.example.QuizQuestArena_Backend.model.Quiz;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * performing database operations related to quiz.
 * Extends CrudRepository to provide basic CRUD functionality for Quiz entity.
 */
public interface QuizRepo extends CrudRepository<Quiz, Long> {
    /**
     * Custom query to fetch detailed quiz scores for each quiz tournament.
     * returning a list of QuizScoreDTO objects with sorted player scores in descending order.
     */
    @Query("SELECT new com.example.QuizQuestArena_Backend.dto.QuizScoreDTO(" +
            "q.id, " +                  // Quiz ID
            "q.name, " +                // The name of the quiz
            "COUNT(p.id), " +    // Total number of players who participated
            "AVG(s.score), " +          // Average score for the quiz
            "q.likes, " +               // Number of likes the quiz received
            "p.id, " +                  // Player ID
            "p.username, " +            // Username of the player
            "s.completedDate, " +       // The date the player completed the quiz
            "s.score) " +               // The player's score
            "FROM Quiz q " +            // The Quiz entity
            "JOIN q.scores s " +        // Join with the Score entity
            "JOIN s.player p " +        // Join with the Player entity
            "GROUP BY q.id, p.id " +    // Grouping by quiz ID and player ID
            "ORDER BY s.score DESC")    // Sorting by player score in descending order
    List<QuizScoreDTO> getQuizScores();
}

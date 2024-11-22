package com.example.QuizQuestArena_Backend.db;

import com.example.QuizQuestArena_Backend.dto.QuizScoreDTO;
import com.example.QuizQuestArena_Backend.model.Question;
import com.example.QuizQuestArena_Backend.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * performing database operations related to quiz.
 * Extends CrudRepository to provide basic CRUD functionality for Quiz entity.
 */
//switched to JpaReposity: Basic CRUD operations are inherited from JpaRepository
public interface QuizRepo extends JpaRepository<Quiz, Long> {
    /**
     * Custom query to fetch detailed quiz scores for each quiz tournament.
     * returning a list of QuizScoreDTO objects with sorted player scores in descending order.
     */
    @Query("SELECT new com.example.QuizQuestArena_Backend.dto.QuizScoreDTO(" +
            "q.id, " +                  // Quiz ID
            "q.name, " +                // The name of the quiz
            "COUNT(s.player.id), " +    // Total number of players who participated
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

    /**
     * Spring Data JPA built-in query methods
     * Fetch ongoing quizzes (startDate <= now <= endDate).
     * Spring boot generate this SQL query dynamically at runtime to fetch from databas
     * [SELECT * FROM quiz WHERE start_date <= ? AND end_date >= ?]
     */
    List<Quiz> findByStartDateBeforeAndEndDateAfter(LocalDateTime now1, LocalDateTime now2);

    /**
     * * Spring Data JPA built-in query methods
     * Fetch upcoming quizzes (startDate > now).
     * SELECT * FROM quiz WHERE start_date > ?;
     */
    List<Quiz> findByStartDateAfter(LocalDateTime now);

    /**
     * * Spring Data JPA built-in query methods
     * Fetch past quizzes (endDate < now).
     * SELECT * FROM quiz WHERE end_date < ?;
     */
    List<Quiz> findByEndDateBefore(LocalDateTime now);

    /** self-customised JPQL query
     * Fetch quizzes participated by a specific player.
     * SELECT q.*
     * FROM quiz q
     * JOIN quiz_participants qp ON q.id = qp.quiz_id
     * JOIN player_user p ON qp.player_id = p.id
     * WHERE p.id = ?;
     */
    @Query("SELECT q FROM Quiz q JOIN q.participants p WHERE p.id = :userId")
    List<Quiz> findParticipatedQuizzesByUserId(@Param("userId") Long userid);

    //for start quiz
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :quizId")
    Optional<Quiz> findByIdWithQuestions(@Param("quizId") Long quizId);

    List<Quiz> findAllById(Long id);

}

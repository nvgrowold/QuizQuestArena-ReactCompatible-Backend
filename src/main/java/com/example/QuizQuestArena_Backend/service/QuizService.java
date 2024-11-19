package com.example.QuizQuestArena_Backend.service;

import com.example.QuizQuestArena_Backend.db.QuizRepo;
import com.example.QuizQuestArena_Backend.dto.QuizScoreDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing quiz-related operations.
 * This layer handles the business logic and communicates with the repository layer to fetch data.
 */

@Service
public class QuizService {

    // Injecting the QuizRepo dependency for database operations related to quizzes.
    @Autowired
    private QuizRepo quizRepo;

    /**
     * Fetches a list of quiz scores with details like player name, score, likes, etc.
     * data retrieved using a custom query in QuizRepo.
     * return A list of QuizScoreDTO objects containing detailed quiz score information.
     */
    public List<QuizScoreDTO> getQuizScores() {
        return quizRepo.getQuizScores();
    }
}

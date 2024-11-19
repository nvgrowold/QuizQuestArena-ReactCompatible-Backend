package com.example.QuizQuestArena_Backend.controller;

import com.example.QuizQuestArena_Backend.dto.QuizScoreDTO;
import com.example.QuizQuestArena_Backend.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

import java.util.List;

/**
 * Controller class for managing quiz-related endpoints.
 * Handles incoming HTTP requests related to quizzes and delegates to QuizService.
 */
@Controller// Use @Controller to enable HTML view rendering, don't use @RestController
public class QuizController {

    @Autowired
    private QuizService quizService;

    LocalDateTime completedDate = LocalDateTime.now(); // Get the current date and time

    /**
     * Endpoint to fetch quiz scores for each tournament.
     * The result includes quiz name, total players participated, average score,
     * number of likes, player’s name, completed date, and player’s score.
     *
     * returning ResponseEntity containing a list of QuizScoreDTO objects
     */
//    @GetMapping("/quizScoresPage")
//    public ResponseEntity<List<QuizScoreDTO>> getQuizScores() {
//        // Fetch quiz scores from service layer
//        List<QuizScoreDTO> scores = quizService.getQuizScores();
//        // Return list of scores wrapped in a ResponseEntity with HTTP status 200 (OK)
//        return ResponseEntity.ok(scores);
//        //return "quizScoresPage"; // Must match the name of the HTML file in the templates directory
//    }

    @GetMapping("/quizScoresPage")
    public String getQuizScoresPage(Model model) {

//        // Temporary mock data
//        List<QuizScoreDTO> scores = List.of(
//                new QuizScoreDTO("Quiz 1", 10, 75.5, 25, "JohnDoe",  LocalDateTime.now(),  85),
//                new QuizScoreDTO("Quiz 2", 5, 65.3, 10, "JaneDoe",  LocalDateTime.now(),  70)
//        );
//        // Fetch scores from the service layer
        List<QuizScoreDTO> scores = quizService.getQuizScores();

        // Add scores to the model so they can be accessed in the Thymeleaf template
        model.addAttribute("scores", scores);

        // Return the name of the HTML template (Thymeleaf will map this to quizScoresPage.html)
        return "quizScoresPage";
    }

}

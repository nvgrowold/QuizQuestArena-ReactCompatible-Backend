package com.example.QuizQuestArena_Backend.controller;

import com.example.QuizQuestArena_Backend.db.UserRepo;
import com.example.QuizQuestArena_Backend.dto.QuizDTO;
import com.example.QuizQuestArena_Backend.dto.QuizScoreDTO;
import com.example.QuizQuestArena_Backend.model.PlayerUser;
import com.example.QuizQuestArena_Backend.model.Quiz;
import com.example.QuizQuestArena_Backend.service.NewQuizNotificationService;
import com.example.QuizQuestArena_Backend.service.QuizService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import java.util.List;

/**
 * Controller class for managing quiz-related endpoints.
 * Handles incoming HTTP requests related to quizzes and delegates to QuizService.
 */
@Controller// Use @Controller to enable HTML view rendering, don't use @RestController
//@RequestMapping("/admin") //admin input submit create quiz  request
@RequestMapping
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private NewQuizNotificationService newQuizNotificationService;

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

        //debug log
        if (scores == null || scores.isEmpty()) {
            System.out.println("No quiz scores fetched.");
        } else {
            System.out.println("Fetched scores: " + scores); // Add debug log
        }

        // Debugging: Print the scores to the console
        for (QuizScoreDTO score : scores) {
            System.out.println("Quiz ID: " + score.getQuizId());
            System.out.println("Quiz Name: " + score.getQuizName());
            System.out.println("Average Score: " + score.getAverageScore());
            System.out.println("Player Name: " + score.getPlayerName());
            System.out.println("Player Score: " + score.getPlayerScore());
            System.out.println("Total Players: " + score.getTotalPlayers());
        }

        // Add scores to the model so they can be accessed in the Thymeleaf template
        model.addAttribute("scores", scores);

        // Return the name of the HTML template (Thymeleaf will map this to quizScoresPage.html)
        return "quizScoresPage";
    }

    @GetMapping("/create-quiz")
    public String getCreateQuizPage(Model model) {
        model.addAttribute("quizDTO", new QuizDTO());
        return "createQuiz";
    }

    @PostMapping("/create-quiz")
     public String createQuiz(@ModelAttribute @Valid QuizDTO quizDTO, BindingResult bindingResult, HttpSession session, Model model) {
//        if (bindingResult.hasErrors()) {
//            // Add validation errors to the model for feedback
//            model.addAttribute("errorMessage", "Validation errors occurred!");
//            model.addAttribute("validationErrors", bindingResult.getFieldErrors());
//            return "createQuiz"; // Return to the form view
//        }

        try {
            // Create the quiz
            Quiz quiz = quizService.createQuiz(quizDTO);
            // Send notification emails
            newQuizNotificationService.sendQuizCreatedEmail(quiz);

            // Ensure user session is valid before redirecting
            Long userId = (Long) session.getAttribute("userId");
            System.out.println("UserId in session: " + userId);
            if (userId == null) {
                return "redirect:/login"; // Redirect to login if session is invalid
            }


            // Fetch user and check role
            PlayerUser user = userRepo.findById(userId).orElse(null);
            if (user == null || !"ROLE_ADMIN".equals(user.getRole())) {
                return "redirect:/userProfile"; // Redirect non-admins to userProfile
            }

            // Add success message and direct to adminProfile
            model.addAttribute("successMessage", "Quiz created successfully!");
            return "adminProfile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create quiz: " + e.getMessage());
            return "createQuiz"; // Return to the form view with an error
        }
    }



    @GetMapping("/manage-quizzes")
    public String manageQuizzes(Model model) {
        List<Quiz> quizzes = quizService.getAllQuizzes();
        model.addAttribute("quizzes", quizzes);
        return "manageQuizzes"; // Thymeleaf template name
    }

    @PostMapping("/update-quiz")
    @ResponseBody
    public ResponseEntity<String> updateQuiz(@RequestBody QuizDTO quizDTO) {
        try {
            quizService.updateQuiz(quizDTO);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @PostMapping("/delete-quiz/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteQuiz(@PathVariable Long id) {
        try {
            quizService.deleteQuiz(id);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("error");
        }
    }

}

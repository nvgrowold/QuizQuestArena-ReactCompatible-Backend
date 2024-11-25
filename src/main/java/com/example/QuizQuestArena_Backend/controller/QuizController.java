package com.example.QuizQuestArena_Backend.controller;

import com.example.QuizQuestArena_Backend.db.QuizRepo;
import com.example.QuizQuestArena_Backend.db.ScoreRepo;
import com.example.QuizQuestArena_Backend.db.UserRepo;
import com.example.QuizQuestArena_Backend.dto.QuizDTO;
import com.example.QuizQuestArena_Backend.dto.QuizScoreDTO;
import com.example.QuizQuestArena_Backend.model.*;
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
import java.util.Optional;

/**
 * Controller class for managing quiz-related endpoints.
 * Handles incoming HTTP requests related to quizzes and delegates to QuizService.
 */
@RestController// Use @Controller to enable HTML view rendering, don't use @RestController
//@RequestMapping("/admin") //admin input submit create quiz  request
@RequestMapping
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private NewQuizNotificationService newQuizNotificationService;

    @Autowired
    private ScoreRepo scoreRepo;

    @Autowired
    private QuizRepo quizRepo;


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

    @GetMapping("/quizScores-Page")
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

        // Return the name of the HTML template (Thymeleaf will map this to quizScores-Page.html)
        return "quizScores-Page";
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
            System.out.println("Email sent for quiz: " + quiz.getId());

            // Ensure user session is valid before redirecting
            Long userId = (Long) session.getAttribute("userId");
            System.out.println("UserId in session: " + userId);
            if (userId == null) {
                return "redirect:/login"; // Redirect to login if session is invalid
            }


            // Fetch user and check role
            PlayerUser user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (user == null || !"ROLE_ADMIN".equals(user.getRole())) {
                return "redirect:/userProfile"; // Redirect non-admins to userProfile
            }

            // Add success message and direct to adminProfile
            model.addAttribute("successMessage", "Quiz created successfully!");
            return "redirect:/adminProfile?userId=" + userId;
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

    //endpoints for player user view all quizzes
    @GetMapping("/viewAllQuizzes")
    public String viewAllQuizzes(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login"; // Redirect to login if session is invalid
        }

        // fetching quizzes by different types
        List<Quiz> ongoingQuizzes = quizService.getOngoingQuizzes();
        List<Quiz> upcomingQuizzes = quizService.getUpcomingQuizzes();
        List<Quiz> pastQuizzes = quizService.getPastQuizzes();
        List<Quiz> participatedQuizzes = quizService.getParticipatedQuizzes(userId);

        // debugging logs
        System.out.println("Fetched Ongoing Quizzes: " + ongoingQuizzes.size());
        System.out.println("Fetched Upcoming Quizzes: " + upcomingQuizzes.size());
        System.out.println("Fetched Past Quizzes: " + pastQuizzes.size());
        System.out.println("Fetched Participated Quizzes: " + participatedQuizzes.size());

        // adding the fetched quizzes to model
        // Model parameter is part of Spring MVC that acts as container
        // for passing data between the controller and the view (HTML templates)
        model.addAttribute("ongoingQuizzes", ongoingQuizzes);
        model.addAttribute("upcomingQuizzes", upcomingQuizzes);
        model.addAttribute("pastQuizzes", pastQuizzes);
        model.addAttribute("participatedQuizzes", participatedQuizzes);

        return "viewAllQuizzes"; // Thymeleaf template name
    }


    //----------------------Ongoing Quiz Functionality---------------------------------
    //start a quiz: display the first question of the quiz.
    @GetMapping("/quizzes/play/{quizId}")
    public String startQuiz(@PathVariable("quizId") Long quizId, HttpSession session, Model model) {
        Optional<Quiz> quizOpt = quizService.getQuizById(quizId);
        if (quizOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Quiz not found!");
            return "error_page";
        }

        Quiz quiz = quizOpt.get();
        List<Question> questions = quiz.getQuestions();

        session.setAttribute("currentQuiz", quiz);
        session.setAttribute("questions", questions);
        session.setAttribute("currentQuestionIndex", 0);
        session.setAttribute("score", 0);

        return "redirect:/quizzes/play/" + quizId + "/question/0";
    }

    // Show the current question
    // Show the current question
    @GetMapping("/quizzes/play/{quizId}/question/{index}")
    public String showQuestion(@PathVariable("quizId") Long quizId,
                               @PathVariable("index") int index,
                               HttpSession session,
                               Model model) {
        // Retrieve quiz and questions from session
        Quiz quiz = (Quiz) session.getAttribute("currentQuiz");
        List<Question> questions = (List<Question>) session.getAttribute("questions");

        // Handle missing quiz or question data
        if (quiz == null || questions == null) {
            model.addAttribute("errorMessage", "Quiz data is missing. Please restart the quiz.");
            return "error_page";
        }

        // Handle invalid index
        if (index < 0 || index >= questions.size()) {
            model.addAttribute("errorMessage", "Invalid question index.");
            return "error_page";
        }

        // Retrieve the current question
        Question question = questions.get(index);

        // Add necessary attributes for rendering the question
        model.addAttribute("question", question); // Correctly add the current question
        model.addAttribute("quiz", quiz);
        model.addAttribute("currentQuestion", question);
        model.addAttribute("currentQuestionIndex", index); // Ensure this matches Thymeleaf expectations
        model.addAttribute("totalQuestions", questions.size());

//        // Retrieve feedback message and clear it from session
//        String feedbackMessage = (String) session.getAttribute("feedbackMessage");
//        if (feedbackMessage != null) {
//            model.addAttribute("feedbackMessage", feedbackMessage);
//            session.removeAttribute("feedbackMessage");
//        }

        // Return the quiz question view
        return "quizQuestion";
    }

    // Submit an answer and navigate to the next question
    @PostMapping("/quizzes/play/{quizId}/question/{index}/submit")
    public String submitAnswer(@PathVariable("quizId") Long quizId,
                               @PathVariable("index") int index,
                               @RequestParam("answer") String answer,
                               HttpSession session,
                               Model model) {
        // Retrieve session data
        List<Question> questions = (List<Question>) session.getAttribute("questions");
        Quiz quiz = (Quiz) session.getAttribute("currentQuiz"); // Retrieve the quiz object
        int score = (int) session.getAttribute("score");

        if (questions == null || index < 0 || index >= questions.size()) {
            model.addAttribute("errorMessage", "Quiz data is missing or invalid.");
            return "redirect:/viewAllQuizzes";
        }

        Question question = questions.get(index);
        // Save user's selected answer
        question.setPlayerAnswer(answer);

        // Check if the answer is correct
        String feedbackMessage;
        if (answer.equalsIgnoreCase(question.getCorrectAnswer())) {
            score++;
            session.setAttribute("score", score);
           // model.addAttribute("feedbackMessage", "Correct!");
            feedbackMessage = "Correct";
        } else {
            feedbackMessage = "Incorrect! The correct answer was: " + question.getCorrectAnswer();
        }

//        // Navigate to the next question or complete the quiz
//        int nextIndex = index + 1;
//        if (nextIndex < questions.size()) {
//            return "redirect:/quizzes/play/" + quizId + "/question/" + nextIndex;
//        } else {
//            return "redirect:/quizzes/play/" + quizId + "/complete";
//        }

        // Add attributes for the current question and feedback
        model.addAttribute("quiz", quiz); // Ensure quiz is added
        model.addAttribute("currentQuestion", question);
        model.addAttribute("currentQuestionIndex", index);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("feedbackMessage", feedbackMessage);

        // Stay on the current question
        return "quizQuestion";
    }

    // Complete the quiz and display the final score
    @GetMapping("/quizzes/play/{quizId}/complete")
    public String completeQuiz(@PathVariable("quizId") Long quizId, HttpSession session, Model model) {

        // Retrieve final score
        int finalScore = (int) session.getAttribute("score");
        List<Question> questions = (List<Question>) session.getAttribute("questions");

        if (questions == null || questions.isEmpty()) {
            model.addAttribute("errorMessage", "No quiz data found. Please try again.");
            return "error_page";
        }

        // Retrieve user ID from session
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login"; // Redirect to login if session is invalid
        }

        // Fetch the player and add to participants list
        PlayerUser user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch the quiz
        Quiz quiz = quizService.getQuizById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Save the user's score to the ScoreRepo
        Score score = new Score();
        score.setScore(finalScore);
        score.setCompletedDate(LocalDateTime.now());
        score.setQuiz(quiz);
        score.setPlayer(user);
        scoreRepo.save(score);

        // Add the user to the quiz's participants
        if (!quiz.getParticipants().contains(user)) {
            quiz.getParticipants().add(user);
            user.getParticipatedQuizzes().add(quiz);

            // Save the updates
            quizRepo.save(quiz);
            userRepo.save(user);
        }

        // Collect question details for feedback
        List<QuizFeedback> feedbackList = questions.stream().map(question -> {
            QuizFeedback feedback = new QuizFeedback();
            feedback.setQuestion(question.getText());
            feedback.setPlayerAnswer(question.getPlayerAnswer());
            feedback.setCorrectAnswer(question.getCorrectAnswer());
            feedback.setCorrect(question.getPlayerAnswer() != null &&
                    question.getPlayerAnswer().equalsIgnoreCase(question.getCorrectAnswer()));
            return feedback;
        }).toList();

        model.addAttribute("finalScore", finalScore);
        model.addAttribute("totalQuestions", questions != null ? questions.size() : 0);
        model.addAttribute("feedbackList", feedbackList);

        session.removeAttribute("currentQuiz");
        session.removeAttribute("questions");
        session.removeAttribute("currentQuestionIndex");
        session.removeAttribute("score");

        //debug
        System.out.println("User added to quiz participants: " + user.getUsername());
        System.out.println("Score saved: " + finalScore);
        System.out.println("Total participants: " + quiz.getParticipants().size());

        return "quizComplete"; // Return the quiz completion view
    }

    //like or dislike option endpoint
    @PostMapping("/quizzes/like-dislike/{quizId}")
    public ResponseEntity<Integer> likeQuiz(@PathVariable Long quizId) {
        Quiz updatedQuiz = quizService.likeQuiz(quizId);
        return ResponseEntity.ok(updatedQuiz.getLikes());
    }

}

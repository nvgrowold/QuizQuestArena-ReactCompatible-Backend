package com.example.QuizQuestArena_Backend.controller;

import com.example.QuizQuestArena_Backend.db.QuizRepo;
import com.example.QuizQuestArena_Backend.db.ScoreRepo;
import com.example.QuizQuestArena_Backend.db.UserRepo;
import com.example.QuizQuestArena_Backend.dto.*;
import com.example.QuizQuestArena_Backend.model.*;
import com.example.QuizQuestArena_Backend.service.NewQuizNotificationService;
import com.example.QuizQuestArena_Backend.service.QuizService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller class for managing quiz-related endpoints.
 * Handles incoming HTTP requests related to quizzes and delegates to QuizService.
 */
@RestController// Use @Controller to enable HTML view rendering, don't use @RestController
//@RequestMapping("/admin") //admin input submit create quiz  request
@RequestMapping("/api/quizzes")
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

    @GetMapping("/quizScoresRanking")
    public ResponseEntity<List<QuizScoreDTO>> getQuizScores() {

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
        return ResponseEntity.ok(scores);
    }

//    @GetMapping("/create-quiz")
//    public String getCreateQuizPage(Model model) {
//        model.addAttribute("quizDTO", new QuizDTO());
//        return "createQuiz";
//    }

    @PostMapping("/create")
     public ResponseEntity<String> createQuiz(@RequestBody @Valid QuizDTO quizDTO, HttpSession session) {
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
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please log in.");
            }

            // Fetch user and check role
            PlayerUser user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!"ROLE_ADMIN".equals(user.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Admins only.");
            }

            return ResponseEntity.ok("Quiz created successfully.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create quiz: " + e.getMessage());
        }
    }

    /**
     * Fetch all quizzes for management.
     */
    @GetMapping("/manage")
    public ResponseEntity<List<QuizDTO>> manageQuizzes() {
        List<QuizDTO> quizzes = quizService.getAllQuizzes().stream()
//                .map(quiz -> new QuizDTO(
//                        quiz.getId(),
//                        quiz.getName(),
//                        quiz.getCategory(),
//                        quiz.getDifficulty(),
//                        quiz.getStartDate(),
//                        quiz.getEndDate(),
//                        quiz.getLikes(),
//                        quiz.getParticipants() != null ? quiz.getParticipants().size() : 0
//                ))
//                .toList();
                .map(QuizDTO::new) // Use the updated QuizDTO
                .toList();
        return ResponseEntity.ok(quizzes);
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateQuiz(@RequestBody QuizDTO quizDTO) {
        try {
            quizService.updateQuiz(quizDTO);
            return ResponseEntity.ok("Quiz updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update quiz: " + e.getMessage());
        }
    }

    //delete by id
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> deleteQuiz(@PathVariable Long id) {
        try {
            quizService.deleteQuiz(id);
            return ResponseEntity.ok("Quiz deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to delete quiz: " + e.getMessage());
        }
    }

    /**
     * View quizzes categorized as ongoing, upcoming, past, and participated.
     */
    //helper method to map Quiz to QuizDTO objects
    //viewAllQuizzes
    private QuizDTO convertToDto(Quiz quiz) {
        return new QuizDTO(
                quiz.getId(),
                quiz.getName(),
                quiz.getCategory(),
                quiz.getDifficulty(),
                quiz.getStartDate(),
                quiz.getEndDate(),
                quiz.getLikes(),
                quiz.getParticipants() != null ? quiz.getParticipants().size() : 0
        );
    }
    //endpoints for player user view all quizzes
    @GetMapping("/viewAllQuizzes")
    public ResponseEntity<?> viewAllQuizzes(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please log in.");
        }

        // fetching quizzes by different types
//        List<Quiz> ongoingQuizzes = quizService.getOngoingQuizzes();
//        List<Quiz> upcomingQuizzes = quizService.getUpcomingQuizzes();
//        List<Quiz> pastQuizzes = quizService.getPastQuizzes();
//        List<Quiz> participatedQuizzes = quizService.getParticipatedQuizzes(userId);

        // Convert quizzes to DTOs to avoid circular references
        List<QuizDTO> ongoingQuizzes = quizService.getOngoingQuizzes()
                .stream()
                .map(this::convertToDto)
                .toList();

        List<QuizDTO> upcomingQuizzes = quizService.getUpcomingQuizzes()
                .stream()
                .map(this::convertToDto)
                .toList();

        List<QuizDTO> pastQuizzes = quizService.getPastQuizzes()
                .stream()
                .map(this::convertToDto)
                .toList();

        List<QuizDTO> participatedQuizzes = quizService.getParticipatedQuizzes(userId)
                .stream()
                .map(this::convertToDto)
                .toList();
        // debugging logs
        System.out.println("Fetched Ongoing Quizzes: " + ongoingQuizzes.size());
        System.out.println("Fetched Upcoming Quizzes: " + upcomingQuizzes.size());
        System.out.println("Fetched Past Quizzes: " + pastQuizzes.size());
        System.out.println("Fetched Participated Quizzes: " + participatedQuizzes.size());

        // Preparing response data
        Map<String, Object> response = new HashMap<>();
        response.put("ongoingQuizzes", ongoingQuizzes);
        response.put("upcomingQuizzes", upcomingQuizzes);
        response.put("pastQuizzes", pastQuizzes);
        response.put("participatedQuizzes", participatedQuizzes);

        return ResponseEntity.ok(response);
    }


    //----------------------Ongoing / Playing Quiz Functionality---------------------------------
    //start a quiz
    //initialise the quiz session:retrieve quiz data, fetches questions and stores in setion.
    //set initial question index and score in session
    @GetMapping("/play/{quizId}")
    public ResponseEntity<?> startQuiz(@PathVariable Long quizId, HttpSession session) {
        Optional<Quiz> quizOpt = quizService.getQuizById(quizId);
        if (quizOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Quiz not found.");
        }

        Quiz quiz = quizOpt.get();
        List<Question> questions = quiz.getQuestions();

        if (questions == null || questions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No questions available for this quiz.");
        }

        session.setAttribute("currentQuiz", quiz);
        session.setAttribute("questions", questions);
        session.setAttribute("currentQuestionIndex", 0);
        session.setAttribute("score", 0);

        // Debugging
        System.out.println("Quiz started: " + quiz.getId());
        System.out.println("Questions loaded: " + questions.size());

        // Prepare response
        // Create DTOs
        QuizDTO quizDTO = new QuizDTO(quiz);
        QuestionDTO firstQuestionDTO = new QuestionDTO(questions.get(0));

        Map<String, Object> response = new HashMap<>();
        response.put("quiz", quizDTO);
        response.put("question", firstQuestionDTO);
        response.put("currentQuestionIndex", 0);
        response.put("totalQuestions", questions.size());

        return ResponseEntity.ok(response);
    }

    // Show the current question
    // Show the current question
    @GetMapping("/play/{quizId}/question/{index}")
    public ResponseEntity<?> showQuestion(@PathVariable Long quizId,@PathVariable int index,HttpSession session) {
        // Retrieve quiz and questions from session
        Quiz quiz = (Quiz) session.getAttribute("currentQuiz");
        List<Question> questions = (List<Question>) session.getAttribute("questions");

        //debug
        if (quiz == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No quiz found in session.");
        }
        if (questions == null || questions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No questions found in session.");
        }
        if (index < 0 || index >= questions.size()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid question index.");
        }

        QuestionDTO questionDTO = new QuestionDTO(questions.get(index));

        // Preparing response
        Map<String, Object> response = new HashMap<>();
        response.put("question", questionDTO);
        response.put("currentQuestionIndex", index);
        response.put("totalQuestions", questions.size());

//        // Retrieve feedback message and clear it from session
//        String feedbackMessage = (String) session.getAttribute("feedbackMessage");
//        if (feedbackMessage != null) {
//            model.addAttribute("feedbackMessage", feedbackMessage);
//            session.removeAttribute("feedbackMessage");
//        }

        return ResponseEntity.ok(response);
    }
    private String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().toLowerCase();
    }


    // Submit an answer and navigate to the next question
    @PostMapping("/play/{quizId}/question/{index}/submit")
    public ResponseEntity<?> submitAnswer(@PathVariable Long quizId,
                               @PathVariable int index,
                               @RequestBody Map<String, String> payload, // Accept answer as JSON payload
                               HttpSession session) {
        String playerAnswer = sanitize(payload.get("answer"));; // Extract answer value from JSON
        // Validate player answer
        if (playerAnswer == null || playerAnswer.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid answer submitted.");
        }
        // Retrieve session data
        List<Question> questions = (List<Question>) session.getAttribute("questions");
        //Quiz quiz = (Quiz) session.getAttribute("currentQuiz"); // Retrieve the quiz object

        if (questions == null || index < 0 || index >= questions.size()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid quiz or question index.");
        }

        Question question = questions.get(index);

        // Debugging: Ensure options are mapped correctly
        System.out.println("Debugging Options:");
        question.getOptions().forEach(option ->
                System.out.println("Option Text: " + sanitize(option.getOptionText()) + ", Is Correct: " + option.isCorrect())
        );

        // Trim and compare answers to avoid case/whitespace issues
        boolean isCorrect = question.getOptions().stream()
                .anyMatch(option -> sanitize(option.getOptionText()).equalsIgnoreCase(sanitize(playerAnswer)) && option.isCorrect());
        // Debugging
        System.out.println("Submitted Answer: " + sanitize(playerAnswer));
        System.out.println("Correct Answer from Question: " + sanitize(question.getCorrectAnswer()));
        System.out.println("Options: ");
        question.getOptions().forEach(option ->
                System.out.println("Option Text: " + sanitize(option.getOptionText()) + ", Is Correct: " + option.isCorrect())
        );
        System.out.println("Comparison Result: " + isCorrect);

        question.setPlayerAnswer(sanitize(playerAnswer)); // Save the sanitized answer
        // Update session score
        int score = (int) session.getAttribute("score");
        if (isCorrect) {
            score++;
            session.setAttribute("score", score);
        }

        // Set player answer for feedback
        //question.setPlayerAnswer(playerAnswer);

        // Return feedback
        String feedbackMessage = isCorrect
                ? "Correct"
                : "Incorrect! The correct answer was: " + sanitize(question.getCorrectAnswer());

//        // Check if the answer is correct
//        if (answer.equalsIgnoreCase(question.getCorrectAnswer())) {
//            score++;
//            session.setAttribute("score", score);
//           // model.addAttribute("feedbackMessage", "Correct!");
//            feedbackMessage = "Correct";
//        } else {
//            feedbackMessage = "Incorrect! The correct answer was: " + question.getCorrectAnswer();
//        }

//        // Navigate to the next question or complete the quiz
//        int nextIndex = index + 1;
//        if (nextIndex < questions.size()) {
//            return "redirect:/quizzes/play/" + quizId + "/question/" + nextIndex;
//        } else {
//            return "redirect:/quizzes/play/" + quizId + "/complete";
//        }

        // Use DTOs to prepare the response
        //QuizDTO quizDTO = new QuizDTO(quiz); // Convert Quiz to QuizDTO
        //QuestionDTO questionDTO = new QuestionDTO(question); // Convert Question to QuestionDTO

        // Preparing response
        Map<String, Object> response = new HashMap<>();
        response.put("quiz", new QuizDTO((Quiz) session.getAttribute("currentQuiz")));
        response.put("currentQuestion", new QuestionDTO(question));
        response.put("currentQuestionIndex", index);
        response.put("totalQuestions", questions.size());
        response.put("feedbackMessage", feedbackMessage);
        response.put("isCorrect", isCorrect);

        System.out.println("Submitted Answer: " + sanitize(playerAnswer));
        System.out.println("Correct Answer: " + sanitize(question.getCorrectAnswer()));
        System.out.println("Comparison Result: " + sanitize(playerAnswer).equalsIgnoreCase(sanitize(question.getCorrectAnswer())));
        System.out.println("Comparison Result: " + isCorrect);

        return ResponseEntity.ok(response);
    }

    // Complete the quiz and display the final score
    @Transactional //ensure the collection is loaded, quiz.getParticipants() call may trigger a lazy loading
    @GetMapping("/play/{quizId}/complete")
    public ResponseEntity<?> completeQuiz(@PathVariable Long quizId, HttpSession session) {

        // Retrieve final score
        Integer finalScore = (int) session.getAttribute("score");
        List<Question> questions = (List<Question>) session.getAttribute("questions");

        if (questions == null || questions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please log in.");
        }

        // Retrieve user ID from session
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session or quiz.");
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

        // Use QuizDTO to avoid lazy initialization issues
        QuizDTO quizDTO = new QuizDTO(quiz); // Convert Quiz entity to QuizDTO

        // Collect question details for feedback
        List<QuizFeedbackDTO> feedbackList = questions.stream()
                .map(question -> {
                            boolean isCorrect = question.getPlayerAnswer() != null &&
                                    question.getOptions().stream()
                                            .anyMatch(option -> option.isCorrect() &&
                                                    sanitize(option.getOptionText()).equalsIgnoreCase(sanitize(question.getPlayerAnswer())));
                            // Debug the feedback
                            System.out.println("Feedback: Question: " + question.getText()
                                    + ", Player Answer: " + sanitize(question.getPlayerAnswer())
                                    + ", Correct Answer: " + sanitize(question.getCorrectAnswer())
                                    + ", Is Correct: " + isCorrect);

                            return new QuizFeedbackDTO(
                                    question.getText(),
                                    question.getPlayerAnswer(),
                                    question.getCorrectAnswer(),
                                    isCorrect
                            );
                })
                .collect(Collectors.toList());

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("finalScore", finalScore);
        response.put("totalQuestions", questions.size());
        response.put("feedbackList", feedbackList);
        response.put("quiz", quizDTO);

        if (session.getAttribute("currentQuiz") != null) {
            session.removeAttribute("currentQuiz");
        }
        if (session.getAttribute("questions") != null) {
            session.removeAttribute("questions");
        }
        if (session.getAttribute("currentQuestionIndex") != null) {
            session.removeAttribute("currentQuestionIndex");
        }
        if (session.getAttribute("score") != null) {
            session.removeAttribute("score");
        }

        //debug
        System.out.println("User added to quiz participants: " + user.getUsername());
        System.out.println("Score saved: " + finalScore);
        System.out.println("Total participants: " + quiz.getParticipants().size());

        return ResponseEntity.ok(response);
    }

    //fetch the current number of likes
    @GetMapping("/{quizId}/likes")
    public ResponseEntity<Integer> getLikes(@PathVariable Long quizId) {
        Quiz quiz = quizService.getQuizById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
        return ResponseEntity.ok(quiz.getLikes());
    }

    //like or dislike option endpoint
    @PostMapping("/{quizId}/like-dislike")
    public ResponseEntity<Integer> likeQuiz(@PathVariable Long quizId, @RequestParam boolean like) {
        Quiz quiz = quizService.getQuizById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));


        if (like) {
            quiz.setLikes(quiz.getLikes() + 1);
        }else {
            quiz.setLikes(Math.max(quiz.getLikes() - 1, 0)); // Optional for dislike
        }

        quizRepo.save(quiz);

        return ResponseEntity.ok(quiz.getLikes());
    }
}

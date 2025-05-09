package com.example.QuizQuestArena_Backend.service;

import com.example.QuizQuestArena_Backend.db.QuestionRepo;
import com.example.QuizQuestArena_Backend.db.QuizRepo;
import com.example.QuizQuestArena_Backend.db.ScoreRepo;
import com.example.QuizQuestArena_Backend.db.UserRepo;
import com.example.QuizQuestArena_Backend.dto.OptionsDTO;
import com.example.QuizQuestArena_Backend.dto.QuestionDTO;
import com.example.QuizQuestArena_Backend.dto.QuizDTO;
import com.example.QuizQuestArena_Backend.dto.QuizScoreDTO;
import com.example.QuizQuestArena_Backend.model.*;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing quiz-related operations.
 * This layer handles the business logic and communicates with the repository layer to fetch data.
 */

@Service
public class QuizService {

    // Injecting the QuizRepo dependency for database operations related to quizzes.
    @Autowired
    private QuizRepo quizRepo;
    @Autowired
    private QuestionRepo questionRepo;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ScoreRepo scoreRepo;

    //used for making HTTP requests to external APIs.
    private final RestTemplate restTemplate = new RestTemplate();

    private NewQuizNotificationService newQuizNotificationService;

    /**
     * Fetches a list of quiz scores with details like player name, score, likes, etc.
     * data retrieved using a custom query in QuizRepo.
     * return A list of QuizScoreDTO objects containing detailed quiz score information.
     */
    public List<QuizScoreDTO> getQuizScores() {

        List<QuizScoreDTO> scores = quizRepo.getQuizScores();
        System.out.println("Quiz Scores: " + scores); // Debug log
        // Debugging: Print the scores to the console
        for (QuizScoreDTO score : scores) {
            System.out.println("Quiz ID: " + score.getQuizId());
            System.out.println("Quiz Name: " + score.getQuizName());
            System.out.println("Average Score: " + score.getAverageScore());
            System.out.println("Player Name: " + score.getPlayerName());
            System.out.println("Player Score: " + score.getPlayerScore());
            System.out.println("Total Players: " + score.getTotalPlayers());
        }

        return scores;
    }

    /**
     * Creates a new quiz based on QuizDTO.
     * Saves the quiz and its associated questions to the database.
     *
     * @param quizDTO Data Transfer Object containing quiz details.
     * @return The created Quiz object.
     */
    public Quiz createQuiz(QuizDTO quizDTO) {
        // Create a new Quiz object and populate it with data from quizDTO.
        Quiz quiz = new Quiz();
        quiz.setName(quizDTO.getName());
        quiz.setCategory(quizDTO.getCategory());
        quiz.setDifficulty(quizDTO.getDifficulty());
        quiz.setStartDate(quizDTO.getStartDate());
        quiz.setEndDate(quizDTO.getEndDate());
        quiz.setLikes(0);

        // Step 1: Save the Quiz entity first
        System.out.println("Saving quiz:" + quiz);

        Quiz savedQuiz = quizRepo.save(quiz);

        System.out.println("Saved quiz with ID:" + savedQuiz.getId());
        if (savedQuiz.getId() == null) {
            throw new RuntimeException("SavedQuiz ID is null!");
        }

        try {
            // Step 2: Fetch and map questions
            // Fetch 10 questions from OpenTDB based on the selected category and difficulty.
            List<Question> questions = fetchQuestions(quizDTO.getCategory(), quizDTO.getDifficulty());
            System.out.println("Fetched Questions: " + questions.size());
            //debug
            if (questions.isEmpty()) {
                throw new RuntimeException("No questions fetched for the quiz.");
            }
            System.out.println("Questions fetched: " + questions.size());
            for (Question question : questions) {
                System.out.println("Question Text: " + question.getText());
            }


            // Step 3: Clear existing questions (if any) and add new ones
            //Avoid Detached Collections
            //Ensure that the questions list in the Quiz entity is initialized and properly managed. If you replace the questions list with a new list (e.g., using savedQuiz.setQuestions(questions)), Hibernate may treat the old list as detached and trigger the error.
            savedQuiz.getQuestions().clear(); // Clear the existing list
            // Link questions to the saved quiz
            for (Question question : questions) {
                question.setQuiz(savedQuiz); // Ensure the relationship is established
                savedQuiz.getQuestions().add(question); // Maintain the bidirectional relationship
                if (question.getQuiz() == null) {
                    //throw new RuntimeException("Question not linked to Quiz!");
                    throw new RuntimeException("Quiz not set for question: " + question.getText());
                }
                System.out.println("*********Question before saving: " + question.getText() + ", Quiz ID: " + question.getQuiz().getId());
                System.out.println("*************Question Text: " + question.getText());
                System.out.println("***************Linked Quiz ID: " + (question.getQuiz() != null ? question.getQuiz().getId() : "null"));
            }

            // Step 4: Save questions.
            questionRepo.saveAll(questions);

            // Step 5: Save the updated quiz with questions
            quizRepo.save(savedQuiz);

            return savedQuiz;

        }catch (Exception e){
            System.err.println("Error during quiz creation: " + e.getMessage());
            throw e; // Re-throw for further debugging
        }
    }

    /**
     * Fetches 10 questions from the OpenTDB API based on category and difficulty.
     *
     * @param category   The category of questions.
     * @param difficulty The difficulty level of questions.
     * @return A list of Question objects.
     */
    private List<Question> fetchQuestions(String category, String difficulty) {
        // Base URL for fetching questions from OpenTDB.
        String baseUrl = "https://opentdb.com/api.php?amount=10";

        // Append category filter if a specific category is selected.
        if (!"Any Category".equals(category)) {
            baseUrl += "&category=" + getCategoryId(category);
        }

        // Append difficulty filter if a specific difficulty is selected.
        if (!"Any difficulty".equals(difficulty)) {
            baseUrl += "&difficulty=" + difficulty.toLowerCase();
        }

        // Make a GET request to OpenTDB API and retrieve the response.
        ResponseEntity<OpenTDBResponse> response = restTemplate.getForEntity(baseUrl, OpenTDBResponse.class);

        // Check if the response is successful and contains a body.
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            // Map the response data to a list of Question objects.
            return mapQuestions(response.getBody().getResults());
        } else {
            throw new RuntimeException("Failed to fetch questions from OpenTDB");
        }
    }

    /**
     * Maps a list of OpenTDBQuestion objects to a list of Question entities.
     *
     * @param openTDBQuestions List of questions and options and answers fetched from OpenTDB API.
     * @return A list of Question entities.
     */
    private List<Question> mapQuestions(List<OpenTDBQuestion> openTDBQuestions) {
        List<Question> questions = new ArrayList<>();
        for (OpenTDBQuestion openTDBQuestion : openTDBQuestions) {
            Question question = new Question();
            question.setText(openTDBQuestion.getQuestion());
            question.setType(openTDBQuestion.getType());
            question.setCorrectAnswer(sanitize(openTDBQuestion.getCorrect_answer())); // Map correct answer
            System.out.println("Fetched Question: " + question.getText());
            System.out.println("Correct Answer: " + sanitize(question.getCorrectAnswer()));

            // Map options (correct and incorrect answers)
            List<Options> options = new ArrayList<>();

            // Add correct answer as an option
            Options correctOption = new Options();
            correctOption.setOptionText(sanitize(openTDBQuestion.getCorrect_answer()));
            correctOption.setCorrect(true); // Mark the correct option
            correctOption.setQuestion(question); // **Set the question reference in Option**
            options.add(correctOption);


            // Add incorrect answers as options
            for (String incorrect : openTDBQuestion.getIncorrect_answers()) {
                Options incorrectOption = new Options();
                incorrectOption.setOptionText(sanitize(incorrect));
                incorrectOption.setCorrect(false);
                incorrectOption.setQuestion(question); // Set question reference
                options.add(incorrectOption);
            }

            // Shuffle options to randomize their order
            Collections.shuffle(options);

            question.setOptions(options); // Set options in question
            questions.add(question);
            // Debugging: Print all options
            System.out.println("Options for Question: " + question.getText());
            // Debugging: Ensure options are mapped correctly
            System.out.println("Mapped Options:");
            for (Options option : options) {
                System.out.println("Option: " + option.getOptionText() + ", Is Correct: " + option.isCorrect());
            }

        }
        return questions;
    }

    /**
     * Sanitizes a string by trimming whitespace and normalizing case.
     * @param input The input string to sanitize.
     * @return A sanitized string.
     */
    private String sanitize(String input) {
        if (input == null) {
            return ""; // Return an empty string for null inputs
        }
        return input.trim().toLowerCase(); // Normalize case and trim whitespace
    }


    /**
     * Maps a human-readable category name to its corresponding category ID in OpenTDB.
     *
     * @param category The category name.
     * @return The corresponding category ID as a string.
     */
    private String getCategoryId(String category) {
        // Mapping of category names to OpenTDB category IDs.
        Map<String, String> categories = Map.of(
                "General Knowledge", "9",
                "Entertainment: Books", "10",
                "Science & Nature", "17",
                "Science: Computers", "18",
                "Science: Mathematics", "19",
                "Sports", "21",
                "Geography", "22",
                "History", "23",
                "Art", "25",
                "Animals", "27"
        );
        // Return the corresponding category ID or an empty string if the category is not mapped.
        return categories.getOrDefault(category, "");
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepo.findAll();
    }

    public void updateQuiz(QuizDTO quizDTO) {
        Quiz quiz = quizRepo.findById(quizDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with id: " + quizDTO.getId()));
        quiz.setName(quizDTO.getName());
        quiz.setCategory(quizDTO.getCategory());
        quiz.setDifficulty(quizDTO.getDifficulty());
        quiz.setStartDate(quizDTO.getStartDate());
        quiz.setEndDate(quizDTO.getEndDate());
        quizRepo.save(quiz);
    }

    public void deleteQuiz(Long id) {
        if (!quizRepo.existsById(id)) {
            throw new IllegalArgumentException("Quiz not found with id: " + id);
        }
        quizRepo.deleteById(id);
    }


    //for playerUser view ongoing, upcoming, past and participated quizzes function
    @Transactional
    public List<Quiz> getOngoingQuizzes() {
        LocalDateTime now = LocalDateTime.now();
        List<Quiz> quizzes = quizRepo.findByStartDateBeforeAndEndDateAfter(now, now)
                .stream()
                .peek(quiz -> Hibernate.initialize(quiz.getParticipants())) // Initialize participants
                .collect(Collectors.toList());
        quizzes.forEach(quiz -> System.out.println("Quiz ID: " + quiz.getId() + ", Participants: " + quiz.getParticipants().size()));
        return quizzes;
    }

    @Transactional
    public List<Quiz> getUpcomingQuizzes() {
        LocalDateTime now = LocalDateTime.now();
        return quizRepo.findByStartDateAfter(now)
                .stream()
                .peek(quiz -> Hibernate.initialize(quiz.getParticipants())) // Initialize participants
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Quiz> getPastQuizzes() {
        LocalDateTime now = LocalDateTime.now();
        return quizRepo.findByEndDateBefore(now)
                .stream()
                .peek(quiz -> Hibernate.initialize(quiz.getParticipants())) // Initialize participants
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Quiz> getParticipatedQuizzes(Long userId) {
        return quizRepo.findParticipatedQuizzesByUserId(userId)
                .stream()
                .peek(quiz -> Hibernate.initialize(quiz.getParticipants())) // Initialize participants
                .collect(Collectors.toList());
    }

    //--------------ongoing quiz function-------------------------------
    public Optional<Quiz> getQuizById(Long quizId) {
       // return quizRepo.findByIdWithQuestions(quizId);
        Optional<Quiz> quizOpt = quizRepo.findByIdWithQuestions(quizId);
        if (quizOpt.isPresent()) {
            Quiz quiz = quizOpt.get();

            // Initialize lazy collections to avoid LazyInitializationException
            quiz.getQuestions().forEach(question -> {
                question.getOptions().size(); // Initialize options
            });

            //debug fetching logic
            for (Question question : quiz.getQuestions()) {
                System.out.println("Question: " + question.getText());
                System.out.println("Correct Answer: " + question.getCorrectAnswer());
                System.out.println("Options: ");
                for (Options option : question.getOptions()) {
                    System.out.println(" - " + option.getOptionText());
                }
            }
        }
        return quizOpt;
    }

    /**
     * Fetches a Quiz and maps it to a QuizDTO with its questions.
     *
     * @param quizId the ID of the quiz to fetch
     * @return an Optional containing the QuizDTO or empty if not found
     */
    public Optional<QuizDTO> getQuizDTOById(Long quizId) {
        return quizRepo.findByIdWithQuestions(quizId)
                .map(quiz -> {
                    QuizDTO dto = new QuizDTO();
                    dto.setId(quiz.getId());
                    dto.setName(quiz.getName());
                    dto.setQuestions(quiz.getQuestions() != null
                            ? quiz.getQuestions().stream()
                            .map(q -> new QuestionDTO(
                                    q.getId(),
                                    q.getText(),
                                    q.getQuestionIndex(),
                                    q.getOptions() != null
                                            ? q.getOptions().stream()
                                            .map(option -> new OptionsDTO(option.getId(), option.getOptionText(), option.isCorrect()))
                                            .collect(Collectors.toList())
                                            : List.of(), // Empty list if options are null
                                    q.getCorrectAnswer()
                            ))
                            .collect(Collectors.toList())
                            : List.of() // Empty list if questions are null
                    );
                    return dto;
                });
    }


    //like dislike option logic
    public Quiz likeQuiz(Long quizId) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        quiz.setLikes(quiz.getLikes() + 1);
        return quizRepo.save(quiz);
    }

    //save user score and add to participants of the quiz after completion of quiz
    @Transactional
    public void saveUserScore(Long quizId, Long userId, int scoreValue) {
        Quiz quiz = quizRepo.findById(quizId).orElseThrow(() -> new RuntimeException("Quiz not found"));

        PlayerUser user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Save score
        Score score = new Score();
        score.setScore(scoreValue);
        score.setCompletedDate(LocalDateTime.now());
        score.setQuiz(quiz);
        score.setPlayer(user);

        scoreRepo.save(score);

        // Update user's total score
        user.setScore(user.getScore() + scoreValue);
        userRepo.save(user);

        // Add the user to quiz participants if not already added
        if (!quiz.getParticipants().contains(user)) {
            quiz.getParticipants().add(user);
            quizRepo.save(quiz);
        }
    }

    public Quiz saveQuiz(Quiz quiz) {
        return quizRepo.save(quiz);
    }
}

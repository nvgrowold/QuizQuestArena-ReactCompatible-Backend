package com.example.QuizQuestArena_Backend.service;

import com.example.QuizQuestArena_Backend.db.QuestionRepo;
import com.example.QuizQuestArena_Backend.db.QuizRepo;
import com.example.QuizQuestArena_Backend.dto.QuizDTO;
import com.example.QuizQuestArena_Backend.dto.QuizScoreDTO;
import com.example.QuizQuestArena_Backend.model.OpenTDBQuestion;
import com.example.QuizQuestArena_Backend.model.OpenTDBResponse;
import com.example.QuizQuestArena_Backend.model.Question;
import com.example.QuizQuestArena_Backend.model.Quiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    //used for making HTTP requests to external APIs.
    private final RestTemplate restTemplate = new RestTemplate();

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

        // Fetch 10 questions from OpenTDB based on the selected category and difficulty.
        List<Question> questions = fetchQuestions(quizDTO.getCategory(), quizDTO.getDifficulty());
        // Save the quiz to the database.
        quizRepo.save(quiz);

        // Save questions and associate them with the quiz
        for (Question question : questions) {
            question.setQuiz(quiz);
            questionRepo.save(question);
        }

        return quiz;
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
     * @param openTDBQuestions List of questions fetched from OpenTDB API.
     * @return A list of Question entities.
     */
    private List<Question> mapQuestions(List<OpenTDBQuestion> openTDBQuestions) {
        List<Question> questions = new ArrayList<>();
        for (OpenTDBQuestion openTDBQuestion : openTDBQuestions) {
            Question question = new Question();
            question.setText(openTDBQuestion.getQuestion());// Map question text.
            question.setType(openTDBQuestion.getType()); // Map question type (e.g., multiple-choice, true/false).
            questions.add(question);
        }
        return questions;
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
}

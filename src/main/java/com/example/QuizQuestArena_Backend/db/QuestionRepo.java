package com.example.QuizQuestArena_Backend.db;

import com.example.QuizQuestArena_Backend.dto.QuizScoreDTO;
import com.example.QuizQuestArena_Backend.model.Question;
import com.example.QuizQuestArena_Backend.model.Quiz;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * performing database operations related to quiz.
 * Extends CrudRepository to provide basic CRUD functionality for Quiz entity.
 */
public interface QuestionRepo extends CrudRepository<Question, Long> {
    List<Question> findAllByQuizId(Long quizId);
}

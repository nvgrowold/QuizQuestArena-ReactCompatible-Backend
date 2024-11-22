package com.example.QuizQuestArena_Backend.service;

import com.example.QuizQuestArena_Backend.db.QuestionRepo;
import com.example.QuizQuestArena_Backend.dto.QuestionDTO;
import com.example.QuizQuestArena_Backend.model.Options;
import com.example.QuizQuestArena_Backend.model.Question;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    private final QuestionRepo questionRepo;

    @Autowired
    public QuestionService(QuestionRepo questionRepo) {
        this.questionRepo = questionRepo;
    }

    @Transactional
    public List<Question> getQuestionsWithOptions(Long quizId) {
        List<Question> questions = questionRepo.findAllByQuizId(quizId);
        questions.forEach(question -> Hibernate.initialize(question.getOptions()));
        return questions;
    }

    //converts Question entities into QuestionDTO objects
    public List<QuestionDTO> getQuestionsForQuiz(Long quizId) {
        List<Question> questions = questionRepo.findAllByQuizId(quizId);
        return questions.stream().map(question ->
                new QuestionDTO(
                    question.getId(),
                    question.getText(),
                    question.getQuestionIndex(),
                    question.getOptions()
                            .stream()
                            .map(Options::getOptionText)
                            .collect(Collectors.toList())
                )
        ).collect(Collectors.toList());
    }
}
package com.example.QuizQuestArena_Backend.db;

import com.example.QuizQuestArena_Backend.model.PlayerUser;
import com.example.QuizQuestArena_Backend.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreRepo extends JpaRepository<Score, Double>  {
}

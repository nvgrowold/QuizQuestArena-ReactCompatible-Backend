package com.example.QuizQuestArena_Backend.db;

import com.example.QuizQuestArena_Backend.model.PasswordReset;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PasswordResetRepo extends CrudRepository<PasswordReset, Long> {
    Optional<PasswordReset> findByToken(String token);
    Optional<PasswordReset> findByEmail(String email);

    //for delete existing key for reseting password request
    void deleteByEmail(String email);
}

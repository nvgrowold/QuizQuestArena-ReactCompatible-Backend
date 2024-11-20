package com.example.QuizQuestArena_Backend.db;

import com.example.QuizQuestArena_Backend.model.PlayerUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends CrudRepository<PlayerUser, Long> {

    Optional<PlayerUser> findByUsername(String username);

    Optional<PlayerUser> findByUsernameAndPassword(String username, String password);

    //for password reset service
    Optional<PlayerUser> findByEmail(String username);

    // Fetch all users with a specific role
    List<PlayerUser> findByRole(String role);
}

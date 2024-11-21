package com.example.QuizQuestArena_Backend.db;

import com.example.QuizQuestArena_Backend.model.PlayerUser;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<PlayerUser, Long> {

    Optional<PlayerUser> findByUsername(String username);

    Optional<PlayerUser> findByUsernameAndPassword(String username, String password);

    //for password reset service
    Optional<PlayerUser> findByEmail(String username);

    // Fetch all users WITHOUT a specific role
    List<PlayerUser> findAllByRoleNot(String role);
}

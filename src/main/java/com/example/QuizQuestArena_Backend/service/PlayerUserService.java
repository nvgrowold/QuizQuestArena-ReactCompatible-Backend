package com.example.QuizQuestArena_Backend.service;

import com.example.QuizQuestArena_Backend.db.PlayerUserRepo;
import com.example.QuizQuestArena_Backend.dto.PlayerUserDTO;
import com.example.QuizQuestArena_Backend.model.PlayerUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayerUserService {

    @Autowired
    private PlayerUserRepo playerUserRepo;

    //register new user method
    public PlayerUser registerPlayerUer(String username, String password, String email){
        // Validate that username and password are not null
        if (username != null && password != null){
            PlayerUser playerUser = new PlayerUser(); //if login and password not null, create a new PlayerUser model
            playerUser.setUsername(username);
            playerUser.setPassword(password);
            playerUser.setEmail(email);
            return playerUserRepo.save(playerUser);//after set up, save the new playeruser to database through playeruserRepo
        }else{
            return null;
        }
    }

    //authentication method
    public PlayerUser authentication(String username, String password){
        // Search the database for a user with the given username and password
        //if found the user in database return it, or else return null
        return playerUserRepo.findByUsernameAndPassword(username,password).orElse(null);
    }

    //update user profile
    public PlayerUser updateUserProfile(PlayerUserDTO playerUserDTO) {
        // Retrieve the user from the database by ID
        Optional<PlayerUser> existingUser = playerUserRepo.findById(playerUserDTO.getId());
        if (existingUser.isPresent()) {
            // If user exists, update the editable fields
            PlayerUser user = existingUser.get();
            user.setFirstName(playerUserDTO.getFirstName());
            user.setLastName(playerUserDTO.getLastName());
            user.setEmail(playerUserDTO.getEmail());
            user.setPhoneNumber(playerUserDTO.getPhoneNumber());
            user.setAddress(playerUserDTO.getAddress());
            user.setRole(playerUserDTO.getRole());
            // Save the updated user to the database and return the updated entity
            return playerUserRepo.save(user);
        }
        // If the user is not found, throw an exception
        throw new RuntimeException("User not found");
    }
}

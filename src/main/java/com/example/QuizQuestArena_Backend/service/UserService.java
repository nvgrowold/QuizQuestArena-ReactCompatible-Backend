package com.example.QuizQuestArena_Backend.service;

import com.example.QuizQuestArena_Backend.db.UserRepo;
import com.example.QuizQuestArena_Backend.dto.UserDTO;
import com.example.QuizQuestArena_Backend.model.PlayerUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    //register new user method
    public PlayerUser registerPlayerUer(String username, String password, String email){
        // Validate that username and password are not null
        if (username != null && password != null){
            PlayerUser playerUser = new PlayerUser(); //if login and password not null, create a new PlayerUser model
            playerUser.setUsername(username);
            playerUser.setPassword(password);
            playerUser.setEmail(email);
            return userRepo.save(playerUser);//after set up, save the new playeruser to database through playeruserRepo
        }else{
            return null;
        }
    }

    //authentication method
    public PlayerUser authentication(String username, String password){
        // Search the database for a user with the given username and password
        //if found the user in database return it, or else return null
        return userRepo.findByUsernameAndPassword(username,password).orElse(null);
    }

    //update user profile
    public PlayerUser updateUserProfile(UserDTO userDTO) {
        // Retrieve the user from the database by ID
        Optional<PlayerUser> existingUser = userRepo.findById(userDTO.getId());
        if (existingUser.isPresent()) {
            // If user exists, update the editable fields
            PlayerUser user = existingUser.get();
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setPhoneNumber(userDTO.getPhoneNumber());
            user.setAddress(userDTO.getAddress());
            user.setRole(userDTO.getRole());
            // Save the updated user to the database and return the updated entity
            return userRepo.save(user);
        }
        // If the user is not found, throw an exception
        throw new RuntimeException("User not found");
    }
}

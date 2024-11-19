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
        //if found the user in database return it, or else return null
        return playerUserRepo.findByUsernameAndPassword(username,password).orElse(null);
    }

    //update user profile
    public PlayerUser updateUserProfile(PlayerUserDTO playerUserDTO) {
        Optional<PlayerUser> existingUser = playerUserRepo.findById(playerUserDTO.getId());
        if (existingUser.isPresent()) {
            PlayerUser user = existingUser.get();
            user.setFirstName(playerUserDTO.getFirstName());
            user.setLastName(playerUserDTO.getLastName());
            user.setEmail(playerUserDTO.getEmail());
            user.setPhoneNumber(playerUserDTO.getPhoneNumber());
            user.setAddress(playerUserDTO.getAddress());
            user.setRole(playerUserDTO.getRole());
            return playerUserRepo.save(user);
        }
        throw new RuntimeException("User not found");
    }
}

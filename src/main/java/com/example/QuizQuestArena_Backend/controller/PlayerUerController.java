package com.example.QuizQuestArena_Backend.controller;

import com.example.QuizQuestArena_Backend.db.PlayerUserRepo;
import com.example.QuizQuestArena_Backend.dto.PlayerUserDTO;
import com.example.QuizQuestArena_Backend.model.PlayerUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession; // Import for HttpSession

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Controller
public class PlayerUerController {

    @Autowired
    private PlayerUserRepo playerUserRepo;

    //methods to redirect user to a specfic page after success logged in
    //register page
    @GetMapping("/register")
    public String getRegisterPage(){
        return "register_page";
    }


    // Handle registration
    @PostMapping("/register")
    public String registerPlayer(@Validated @ModelAttribute PlayerUserDTO playerUserDTO,
                                 BindingResult result, Model model) {

        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Validation errors occurred.");
            return "register_page";
        }

        try {
            // Check if username already exists
            Optional<PlayerUser> existingUser = playerUserRepo.findByUsername(playerUserDTO.getUsername());
            if (existingUser.isPresent()) {
                model.addAttribute("errorMessage", "User with the same username already exists!");
                return "register_page";
            }

            // Handle profile picture if provided
            String profilePictureUrl = null;
            if (playerUserDTO.getProfilePicture() != null && !playerUserDTO.getProfilePicture().isEmpty()) {
                profilePictureUrl = saveProfilePicture(playerUserDTO.getProfilePicture());
            }

            // Map DTO to entity and save
            PlayerUser playerUser = mapToEntity(playerUserDTO);
            playerUser.setProfilePicture(profilePictureUrl); // Set the URL or leave null
            playerUserRepo.save(playerUser);

            model.addAttribute("successMessage", "Registration successful! Please log in.");
            return "login_page";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred during registration: " + e.getMessage());
            return "register_page";
        }
    }

    //Helper method
    private String saveProfilePicture(MultipartFile file) throws Exception {
        // Define the directory where files will be saved
        String uploadDir = "uploads/";
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename(); // Unique file name
        Path filePath = Paths.get(uploadDir + fileName);

        // Ensure the directory exists
        Files.createDirectories(filePath.getParent());

        // Save the file to the directory
        Files.write(filePath, file.getBytes());

        // Return the file path or URL to save in the database
        return filePath.toString();
    }

    //login page
    @GetMapping("/login")
    public String getLoginPage(){
        return "login_page";
    }

    //handle login requests
    @PostMapping("/login")
    public String loginPlayer(@ModelAttribute PlayerUserDTO playerUserDTO, Model model,  HttpSession session) {
        System.out.println("Attempting login for username: " + playerUserDTO.getUsername());

        //search and compare in the database for the username and password
        Optional<PlayerUser> authenticatedUser = playerUserRepo.findByUsernameAndPassword(
                playerUserDTO.getUsername(), playerUserDTO.getPassword());

        if (authenticatedUser.isPresent()) {
           // model.addAttribute("successMessage", "Login Successful!"); // Add success message
            model.addAttribute("playerUser", authenticatedUser.get()); // Pass the user data to the profile page
            System.out.println("Login successful for user: " + playerUserDTO.getUsername());
            session.setAttribute("userId", authenticatedUser.get().getId()); // Store userId in session
            return "redirect:/userProfile"; // User ID is stored in session
            //return "redirect:/userProfile?userId=" + authenticatedUser.get().getId(); // Pass userId in the redirect
        } else {
            model.addAttribute("errorMessage", "Invalid login or password!");
            System.out.println("Login failed for user: " + playerUserDTO.getUsername());
            return "login_page";
        }
    }

    // Utility: Map DTO to Entity. The input DTO is converted into an entity for saving to the database
    private PlayerUser mapToEntity(PlayerUserDTO dto) {
        return new PlayerUser(
                null, // ID will be auto-generated
                dto.getUsername(),
                dto.getPassword(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getEmail(),
                dto.getPhoneNumber(),
                dto.getAddress(),
                dto.getRole(),
                0, // Initial score
                null // Profile picture will be set later if uploaded
        );
    }


    // Utility: Map Entity to DTO. The output DTO is constructed from the entity after retrieval from the database
    private PlayerUserDTO mapToDTO(PlayerUser entity) {
        return new PlayerUserDTO(
                entity.getId(),               // ID
                entity.getUsername(),         // Username
                entity.getFirstName(),        // First name
                entity.getLastName(),         // Last name
                entity.getEmail(),            // Email
                entity.getPhoneNumber(),      // Phone number
                entity.getAddress(),          // Address
                entity.getRole(),             // Role
                entity.getProfilePicture()    // Profile picture URL as String
        );
    }

    //update user profile function____________________________________________
    // Display user profile
    @GetMapping("/userProfile")
    public String getUserProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId"); //get user id during valid session
        System.out.println("Retrieved userId from session: " + userId);

        //search user by id in the database
        Optional<PlayerUser> user = playerUserRepo.findById(userId);
        if (user.isPresent()) {
            model.addAttribute("playerUser", user.get());
            return "userProfile";
        } else {
            model.addAttribute("errorMessage", "User not found!");
            return "error_page";
        }
    }

    // Update user profile
    @PostMapping("/updateProfile/{id}")
    public String updateUserProfile(@PathVariable("id") Long id, @ModelAttribute PlayerUserDTO playerUserDTO, Model model) {
        Optional<PlayerUser> existingUser = playerUserRepo.findById(playerUserDTO.getId());
        if (existingUser.isPresent()) {
            PlayerUser user = existingUser.get();
            // Update editable fields
            user.setFirstName(playerUserDTO.getFirstName());
            user.setLastName(playerUserDTO.getLastName());
            user.setEmail(playerUserDTO.getEmail());
            user.setPhoneNumber(playerUserDTO.getPhoneNumber());
            user.setAddress(playerUserDTO.getAddress());
            user.setRole(playerUserDTO.getRole());

            playerUserRepo.save(user); // Save updated user
            model.addAttribute("successMessage", "Profile updated successfully!");
            model.addAttribute("playerUser", user);
            // Redirect to the user's profile page with their userId
            return "redirect:/userProfile?userId=" + user.getId();
        } else {
            model.addAttribute("errorMessage", "User not found!");
            return "error_page";
        }
    }

    //logout from userprofile page
    @GetMapping("/logout")
    public String logoutUser(HttpSession session) {
        // Invalidate session or perform logout logic here
        if (session != null) {
            session.invalidate(); // Invalidate the user's session
        }
        return "redirect:/"; // Redirect to the home page
    }

}

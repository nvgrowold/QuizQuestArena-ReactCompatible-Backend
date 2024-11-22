package com.example.QuizQuestArena_Backend.controller;

import com.example.QuizQuestArena_Backend.db.UserRepo;
import com.example.QuizQuestArena_Backend.dto.UserDTO;
import com.example.QuizQuestArena_Backend.model.PlayerUser;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserController {

    @Autowired
    private UserRepo userRepo;

    //methods to redirect user to a specfic page after success logged in
    //register page
    @GetMapping("/register")
    public String getRegisterPage(){
        return "register_page";
    }


    // Handle registration
    @PostMapping("/register")
    public String registerPlayer(@Validated @ModelAttribute UserDTO userDTO,
                                 BindingResult result, Model model) {

        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Validation errors occurred.");
            return "register_page";
        }

        try {
            // Check if username already exists
            Optional<PlayerUser> existingUser = userRepo.findByUsername(userDTO.getUsername());
            if (existingUser.isPresent()) {
                model.addAttribute("errorMessage", "User with the same username already exists!");
                return "register_page";
            }

            // Handle profile picture if provided
            String profilePictureUrl = null;
            if (userDTO.getProfilePicture() != null && !userDTO.getProfilePicture().isEmpty()) {
                profilePictureUrl = saveProfilePicture(userDTO.getProfilePicture());
            }

            // Map DTO to entity and save
            PlayerUser playerUser = mapToEntity(userDTO);
            playerUser.setProfilePicture(profilePictureUrl); // Set the URL or leave null
            userRepo.save(playerUser);

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
    public String loginPlayer(@ModelAttribute UserDTO userDTO, Model model, HttpSession session) {
        System.out.println("Attempting login for username: " + userDTO.getUsername());

        //search and compare in the database for the username and password
        Optional<PlayerUser> authenticatedUser = userRepo.findByUsernameAndPassword(
                userDTO.getUsername(), userDTO.getPassword());

        if (authenticatedUser.isPresent()) {
            PlayerUser user = authenticatedUser.get();
            session.setAttribute("userId", user.getId()); // Store userId in session
           // model.addAttribute("successMessage", "Login Successful!"); // Add success message
            model.addAttribute("playerUser", mapToDTO(user));
            System.out.println("Login successful for user: " + userDTO.getUsername());
            return "redirect:/userProfile"; // User ID is stored in session
        } else {
            model.addAttribute("errorMessage", "Invalid login or password!");
            System.out.println("Session is null or expired. Redirecting to login.");
            System.out.println("Login failed for user: " + userDTO.getUsername());
            return "redirect:/login";
        }
    }

    // Utility: Map DTO to Entity. The input DTO is converted into an entity for saving to the database
    private PlayerUser mapToEntity(UserDTO dto) {
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
    private UserDTO mapToDTO(PlayerUser entity) {
        return new UserDTO(
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
        if (userId == null) {
            return "redirect:/login"; // Redirect to login if no session
        }

        //search user by id in the database
        Optional<PlayerUser> userOptional = userRepo.findById(userId);
        if (userOptional.isPresent()) {
            PlayerUser user = userOptional.get();
            model.addAttribute("playerUser", mapToDTO(user));

            System.out.println("PlayerUser object: " + user);

            // Redirect based on user role
            return "ROLE_ADMIN".equals(user.getRole()) ? "adminProfile" : "userProfile";
        }
        model.addAttribute("errorMessage", "User not found!");
        return "error_page";
    }

    @GetMapping("/adminProfile")
    public String adminProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId"); // Fetch user ID from the session
        System.out.println("Retrieved userId from session: " + userId);
        if (userId == null) {
            model.addAttribute("errorMessage", "No active session. Please log in.");
            return "redirect:/login"; // Redirect to login if no session
        }

        // Fetch the user from the database
        Optional<PlayerUser> userOptional = userRepo.findById(userId);

        if (userOptional.isPresent()) {
            PlayerUser user = userOptional.get();
            System.out.println("User found: " + user);
            model.addAttribute("playerUser", user); // Add user to the model

            // Add debug log to check if the object is added to the model
            System.out.println("AdminUser in adminProfile: " + user);
            return "adminProfile"; // Return the adminProfile view
        } else {
            model.addAttribute("errorMessage", "User not found!");
            return "error_page"; // Handle the case where the user is not found
        }
    }

    // Update user profile
    @PostMapping("/updateProfile/{id}")
    public String updateUserProfile(@PathVariable("id") Long id, @ModelAttribute UserDTO userDTO, Model model) {
        Optional<PlayerUser> existingUser = userRepo.findById(userDTO.getId());
        if (existingUser.isPresent()) {
            PlayerUser user = existingUser.get();
            // Update editable fields
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setPhoneNumber(userDTO.getPhoneNumber());
            user.setAddress(userDTO.getAddress());
            //user.setRole(userDTO.getRole());

            userRepo.save(user); // Save updated user
            model.addAttribute("successMessage", "Profile updated successfully!");
            model.addAttribute("playerUser", user);
            // Redirect based on role
            if ("ROLE_ADMIN".equals(user.getRole())) {
                return "redirect:/adminProfile?userId=" + user.getId();
            } else {
                return "redirect:/userProfile?userId=" + user.getId();
            }
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

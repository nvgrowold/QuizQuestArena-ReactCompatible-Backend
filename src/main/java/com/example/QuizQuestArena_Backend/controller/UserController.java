package com.example.QuizQuestArena_Backend.controller;

import com.example.QuizQuestArena_Backend.db.UserRepo;
import com.example.QuizQuestArena_Backend.dto.UserDTO;
import com.example.QuizQuestArena_Backend.model.PlayerUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession; // Import for HttpSession

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/api/users") // Base path for user-related endpoints
public class UserController {

    @Autowired
    private UserRepo userRepo;

//    //methods to redirect user to a specfic page after success logged in
//    //register page
//    @GetMapping("/register")
//    public String getRegisterPage(){
//        return "register_page";
//    }


    // Handle registration
    @PostMapping("/register")
    public ResponseEntity<?> registerPlayer(
//            @RequestPart("profilePicture") MultipartFile profilePicture, // Handle file
            @RequestBody UserDTO userDTO) {                     // Handle other fields
        try {

//            // Process file upload
//            String profilePictureUrl = null;
//            if (profilePicture != null && !profilePicture.isEmpty()) {
//                profilePictureUrl = saveProfilePicture(profilePicture);
//            }

//            // Map DTO to entity
            PlayerUser playerUser = mapToEntity(userDTO);
//            playerUser.setProfilePicture(profilePictureUrl); // Set file path

            // Check if username already exists
            Optional<PlayerUser> existingUser = userRepo.findByUsername(userDTO.getUsername());
            if (existingUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("User with the same username already exists!");
            }

            userRepo.save(playerUser);

            return ResponseEntity.status(HttpStatus.CREATED).body("Registration successful!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during registration: " + e.getMessage());
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

//    //login page
//    @GetMapping("/login")
//    public String getLoginPage(){
//        return "login_page";
//    }

    //handle login requests
    @PostMapping("/login")
    public ResponseEntity<?> loginPlayer(@RequestBody UserDTO userDTO, HttpSession session) {
        System.out.println("Attempting login for username: " + userDTO.getUsername());

        //search and compare in the database for the username and password
        Optional<PlayerUser> authenticatedUser = userRepo.findByUsernameAndPassword(
                userDTO.getUsername(), userDTO.getPassword());

        if (authenticatedUser.isPresent()) {
            PlayerUser user = authenticatedUser.get();
            session.setAttribute("userId", user.getId()); // Store userId in session
           // model.addAttribute("successMessage", "Login Successful!"); // Add success message
            System.out.println("Login successful for user: " + userDTO.getUsername());
            return ResponseEntity.ok(mapToDTO(user)); // Return user details
        } else {
            System.out.println("Session is null or expired. Redirecting to login.");
            System.out.println("Login failed for user: " + userDTO.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password!");
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
    public ResponseEntity<?> getUserProfile(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId"); //get user id during valid session
        System.out.println("Retrieved userId from session: " + userId);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No active session. Please log in.");
        }

        //search user by id in the database
        Optional<PlayerUser> userOptional = userRepo.findById(userId);
        if (userOptional.isPresent()) {
            PlayerUser user = userOptional.get();
            System.out.println("PlayerUser object: " + user);
            return ResponseEntity.ok(mapToDTO(user)); // Return user profile
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found!");
        }
    }

    @GetMapping("/adminProfile")
    public  ResponseEntity<?> adminProfile(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId"); // Fetch user ID from the session
        System.out.println("Retrieved userId from session: " + userId);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No active session. Please log in.");
        }

        // Fetch the user from the database
        Optional<PlayerUser> userOptional = userRepo.findById(userId);

        if (userOptional.isPresent()) {
            PlayerUser user = userOptional.get();
            System.out.println("User found: " + user);
            if ("ROLE_ADMIN".equals(user.getRole())) {
                return ResponseEntity.ok(mapToDTO(user));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied: Not an admin user.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found!");
        }
    }

    // Update user profile
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable("id") Long id,
            @RequestBody UserDTO userDTO
//            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture
   ) {
        try {
            Optional<PlayerUser> existingUser = userRepo.findById(id);
            if (existingUser.isPresent()) {
                PlayerUser user = existingUser.get();

                // Update editable fields
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                user.setEmail(userDTO.getEmail());
                user.setPhoneNumber(userDTO.getPhoneNumber());
                user.setAddress(userDTO.getAddress());

                userRepo.save(user); // Save updated user
                return ResponseEntity.ok("Profile updated successfully!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
            }
        } catch (Exception e) {
            // Log exception stack trace
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }



    //logout from userprofile page
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpSession session) {
        // Invalidate session or perform logout logic here
        session.invalidate(); //invalidate user's session
        return ResponseEntity.ok("Logged out successfully!");
    }

}

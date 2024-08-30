package com.spring_boot.blog_application.controller;

import com.spring_boot.blog_application.config.EmailService;
import com.spring_boot.blog_application.entity.User;
import com.spring_boot.blog_application.service.UserDetailsServiceImpl;
import com.spring_boot.blog_application.service.UserService;
import com.spring_boot.blog_application.utils.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import com.spring_boot.blog_application.config.EmailService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = { "https://story-sync-frontend.vercel.app/" }) // Allow CORS for specific origins
@RequestMapping("/public")
public class PublicController {
    private static final Logger log = LoggerFactory.getLogger(PublicController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JWTUtil jwtUtil;

    @GetMapping("/health-check")
    public String healthCheck() {
        return "Ok";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // @PostMapping("/signup")
    // public ResponseEntity<?> registerUser(@RequestBody User user) {
    // try {
    // // Perform registration logic (e.g., save the user to the database)
    // userService.saveNewUser(user);

    // // Return success response with the user details
    // return new ResponseEntity<>("Registration Successful", HttpStatus.CREATED);

    // } catch (Exception e) {
    // // Log the exception (optional)
    // e.printStackTrace();

    // // Return an error response
    // return new ResponseEntity<>("Registration failed",
    // HttpStatus.INTERNAL_SERVER_ERROR);
    // }
    // }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // Save the new user in the database
            userService.saveNewUser(user);

            // Send a welcome email after successful registration
            String to = user.getEmail();
            String subject = "Welcome to StorySync!";

            // Create an HTML formatted email body
            String body = "Dear " + user.getUserName() + ",\n\n" +
                    "Welcome to StorySync!\n\n" +
                    "Thank you for joining StorySync. We're thrilled to have you as part of our community of passionate bloggers and readers.\n\n" +
                    "Get started by exploring stories, sharing your own, and connecting with others who share your interests. If you have any questions or need assistance, feel free to reach out to us.\n\n" +
                    "Best regards,\n" +
                    "The StorySync Team";


            // Use the email service to send the HTML email
            emailService.sendEmail(to, subject, body);

            // Return success response
            return new ResponseEntity<>("Registration Successful", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Registration Failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword()));
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserName());
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            return new ResponseEntity<>(jwt, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            log.error("Incorrect username or password: {}", e.getMessage());
            return new ResponseEntity<>("Incorrect username or password", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Exception occurred while creating authentication token: {}", e.getMessage(), e);
            return new ResponseEntity<>("Authentication failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
package com.example.QuizQuestArena_Backend.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, String>> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Something went wrong!");

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                response.put("message", "Page not found!");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                response.put("message", "Internal server error!");
            }
                response.put("status", statusCode.toString());
            } else {
                response.put("status", "UNKNOWN");
            }
             return ResponseEntity.status((status != null) ? (Integer) status : HttpStatus.INTERNAL_SERVER_ERROR.value()).body(response);
        }
    }


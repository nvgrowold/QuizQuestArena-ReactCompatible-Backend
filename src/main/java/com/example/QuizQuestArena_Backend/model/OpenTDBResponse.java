package com.example.QuizQuestArena_Backend.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OpenTDBResponse {
    private int response_code;
    private List<OpenTDBQuestion> results;

}

package com.example.quizapp.controller;

import com.example.quizapp.dto.QuizSessionResponse;
import com.example.quizapp.dto.QuizStatsResponse;
import com.example.quizapp.dto.ScoreResponse;
import com.example.quizapp.dto.SubmitAnswersRequest;
import com.example.quizapp.service.QuizSessionService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/sessions")
public class StudentSessionController {

    private final QuizSessionService quizSessionService;

    public StudentSessionController(QuizSessionService quizSessionService) {
        this.quizSessionService = quizSessionService;
    }

    @GetMapping("/{accessCode}")
    public ResponseEntity<QuizSessionResponse> getSession(@PathVariable String accessCode) {
        return ResponseEntity.ok(quizSessionService.getSessionForStudent(accessCode));
    }

    @PostMapping("/{accessCode}/answers")
    public ResponseEntity<QuizStatsResponse> submitAnswers(@PathVariable String accessCode,
                                                           @Valid @RequestBody SubmitAnswersRequest request,
                                                           Principal principal) {
        QuizStatsResponse response = quizSessionService.submitAnswers(accessCode, principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accessCode}/score")
    public ResponseEntity<ScoreResponse> getScore(@PathVariable String accessCode, Principal principal) {
        return ResponseEntity.ok(quizSessionService.getScore(accessCode, principal.getName()));
    }
}

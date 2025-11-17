package com.example.quizapp.controller;

import com.example.quizapp.dto.QuizSessionRequest;
import com.example.quizapp.dto.QuizSessionResponse;
import com.example.quizapp.dto.QuizStatsResponse;
import com.example.quizapp.dto.StudentScoreDto;
import com.example.quizapp.service.QuizSessionService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/professor/sessions")
public class ProfessorSessionController {

    private final QuizSessionService quizSessionService;

    public ProfessorSessionController(QuizSessionService quizSessionService) {
        this.quizSessionService = quizSessionService;
    }

    @GetMapping
    public ResponseEntity<List<QuizSessionResponse>> listSessions(Principal principal) {
        return ResponseEntity.ok(quizSessionService.listSessions(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<QuizSessionResponse> createSession(@Valid @RequestBody QuizSessionRequest request,
                                                             Principal principal) {
        QuizSessionResponse response = quizSessionService.createSession(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{sessionId}")
    public ResponseEntity<QuizSessionResponse> updateSession(@PathVariable Long sessionId,
                                                             @Valid @RequestBody QuizSessionRequest request,
                                                             Principal principal) {
        QuizSessionResponse response = quizSessionService.updateSession(sessionId, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/activate")
    public ResponseEntity<QuizSessionResponse> activateSession(@PathVariable Long sessionId, Principal principal) {
        return ResponseEntity.ok(quizSessionService.activateSession(sessionId, principal.getName()));
    }

    @PostMapping("/{sessionId}/close")
    public ResponseEntity<QuizSessionResponse> closeSession(@PathVariable Long sessionId, Principal principal) {
        return ResponseEntity.ok(quizSessionService.closeSession(sessionId, principal.getName()));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId, Principal principal) {
        quizSessionService.deleteSession(sessionId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{sessionId}/questions/{questionId}/stats")
    public ResponseEntity<QuizStatsResponse> getStats(@PathVariable Long sessionId,
                                                      @PathVariable Long questionId,
                                                      Principal principal) {
        return ResponseEntity.ok(quizSessionService.getStats(sessionId, questionId, principal.getName()));
    }

    @GetMapping("/{sessionId}/scores")
    public ResponseEntity<List<StudentScoreDto>> listScores(@PathVariable Long sessionId, Principal principal) {
        return ResponseEntity.ok(quizSessionService.listScores(sessionId, principal.getName()));
    }
}

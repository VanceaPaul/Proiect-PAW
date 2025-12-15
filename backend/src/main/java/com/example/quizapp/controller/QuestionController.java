package com.example.quizapp.controller;

import com.example.quizapp.dto.QuestionRequest;
import com.example.quizapp.dto.QuestionResponse;
import com.example.quizapp.service.QuestionService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> listQuestions(Principal principal) {
        return ResponseEntity.ok(questionService.listQuestions(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> createQuestion(@Valid @RequestBody QuestionRequest request,
                                                           Principal principal) {
        QuestionResponse response = questionService.createQuestion(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(@PathVariable Long id,
                                                           @Valid @RequestBody QuestionRequest request,
                                                           Principal principal) {
        QuestionResponse response = questionService.updateQuestion(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id, Principal principal) {
        questionService.deleteQuestion(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}

package com.example.quizapp.service;

import com.example.quizapp.dto.QuestionRequest;
import com.example.quizapp.dto.QuestionResponse;
import java.util.List;

public interface QuestionService {
    QuestionResponse createQuestion(QuestionRequest request, String professorEmail);
    QuestionResponse updateQuestion(Long id, QuestionRequest request, String professorEmail);
    void deleteQuestion(Long id, String professorEmail);
    List<QuestionResponse> listQuestions(String professorEmail);
}

package com.example.quizapp.service;

import com.example.quizapp.dto.QuizSessionRequest;
import com.example.quizapp.dto.QuizSessionResponse;
import com.example.quizapp.dto.ScoreResponse;
import com.example.quizapp.dto.StudentScoreDto;
import com.example.quizapp.dto.SubmitAnswersRequest;
import com.example.quizapp.dto.QuizStatsResponse;
import java.util.List;

public interface QuizSessionService {
    QuizSessionResponse createSession(QuizSessionRequest request, String professorEmail);
    QuizSessionResponse activateSession(Long sessionId, String professorEmail);
    QuizSessionResponse closeSession(Long sessionId, String professorEmail);
    QuizSessionResponse updateSession(Long sessionId, QuizSessionRequest request, String professorEmail);
    void deleteSession(Long sessionId, String professorEmail);
    List<QuizSessionResponse> listSessions(String professorEmail);
    QuizSessionResponse getSessionForStudent(String accessCode);
    QuizStatsResponse submitAnswers(String accessCode, String studentEmail, SubmitAnswersRequest request);
    ScoreResponse getScore(String accessCode, String studentEmail);
    QuizStatsResponse getStats(Long sessionId, Long questionId, String professorEmail);
    List<StudentScoreDto> listScores(Long sessionId, String professorEmail);
}

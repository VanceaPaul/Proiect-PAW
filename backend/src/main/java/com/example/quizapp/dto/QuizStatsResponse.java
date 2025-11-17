package com.example.quizapp.dto;

import java.util.List;

public class QuizStatsResponse {

    private Long sessionId;
    private Long questionId;
    private long totalAnswers;
    private List<AnswerDistributionDto> distribution;

    public QuizStatsResponse() {
    }

    public QuizStatsResponse(Long sessionId, Long questionId, long totalAnswers, List<AnswerDistributionDto> distribution) {
        this.sessionId = sessionId;
        this.questionId = questionId;
        this.totalAnswers = totalAnswers;
        this.distribution = distribution;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public long getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(long totalAnswers) {
        this.totalAnswers = totalAnswers;
    }

    public List<AnswerDistributionDto> getDistribution() {
        return distribution;
    }

    public void setDistribution(List<AnswerDistributionDto> distribution) {
        this.distribution = distribution;
    }
}

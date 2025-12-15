package com.example.quizapp.dto;

import java.util.List;

public class ScoreResponse {

    private Long sessionId;
    private int correct;
    private int total;
    private List<QuestionScoreDto> results;

    public ScoreResponse() {
    }

    public ScoreResponse(Long sessionId, int correct, int total, List<QuestionScoreDto> results) {
        this.sessionId = sessionId;
        this.correct = correct;
        this.total = total;
        this.results = results;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<QuestionScoreDto> getResults() {
        return results;
    }

    public void setResults(List<QuestionScoreDto> results) {
        this.results = results;
    }
}

package com.example.quizapp.dto;

import java.util.List;

public class QuestionScoreDto {

    private Long questionId;
    private String question;
    private boolean correct;
    private boolean answered;
    private Long selectedOptionId;
    private List<Long> correctOptionIds;

    public QuestionScoreDto() {
    }

    public QuestionScoreDto(Long questionId, String question, boolean correct, boolean answered,
                             Long selectedOptionId, List<Long> correctOptionIds) {
        this.questionId = questionId;
        this.question = question;
        this.correct = correct;
        this.answered = answered;
        this.selectedOptionId = selectedOptionId;
        this.correctOptionIds = correctOptionIds;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public List<Long> getCorrectOptionIds() {
        return correctOptionIds;
    }

    public void setCorrectOptionIds(List<Long> correctOptionIds) {
        this.correctOptionIds = correctOptionIds;
    }
}

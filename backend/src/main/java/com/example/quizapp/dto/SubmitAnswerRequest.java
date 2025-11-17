package com.example.quizapp.dto;

import jakarta.validation.constraints.NotNull;

public class SubmitAnswerRequest {

    @NotNull
    private Long questionId;

    @NotNull
    private Long optionId;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }
}

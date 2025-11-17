package com.example.quizapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SubmitAnswersRequest {

    @Valid
    @NotEmpty
    private List<SubmitAnswerRequest> answers;

    public List<SubmitAnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SubmitAnswerRequest> answers) {
        this.answers = answers;
    }
}

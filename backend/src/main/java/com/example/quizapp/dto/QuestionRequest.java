package com.example.quizapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class QuestionRequest {

    @NotBlank
    private String text;

    @Valid
    @Size(min = 2, message = "A question must have at least two answer options")
    private List<AnswerOptionDto> options;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<AnswerOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<AnswerOptionDto> options) {
        this.options = options;
    }
}

package com.example.quizapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public class QuizSessionRequest {

    @NotBlank
    private String title;

    @Min(60)
    private Integer durationSeconds;

    @NotEmpty
    private Set<Long> questionIds;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Set<Long> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(Set<Long> questionIds) {
        this.questionIds = questionIds;
    }
}

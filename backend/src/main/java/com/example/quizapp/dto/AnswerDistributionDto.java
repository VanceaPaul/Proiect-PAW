package com.example.quizapp.dto;

public class AnswerDistributionDto {

    private Long optionId;
    private long count;

    public AnswerDistributionDto() {
    }

    public AnswerDistributionDto(Long optionId, long count) {
        this.optionId = optionId;
        this.count = count;
    }

    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

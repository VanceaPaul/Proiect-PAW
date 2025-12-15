package com.example.quizapp.dto;

public class StudentScoreDto {

    private Long studentId;
    private String studentName;
    private String studentEmail;
    private int correct;
    private int total;
    private boolean submitted;

    public StudentScoreDto() {
    }

    public StudentScoreDto(Long studentId,
                           String studentName,
                           String studentEmail,
                           int correct,
                           int total,
                           boolean submitted) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.correct = correct;
        this.total = total;
        this.submitted = submitted;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
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

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }
}

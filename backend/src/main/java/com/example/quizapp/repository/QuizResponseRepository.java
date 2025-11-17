package com.example.quizapp.repository;

import com.example.quizapp.entity.Question;
import com.example.quizapp.entity.QuizResponse;
import com.example.quizapp.entity.QuizSession;
import com.example.quizapp.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizResponseRepository extends JpaRepository<QuizResponse, Long> {
    List<QuizResponse> findAllBySession(QuizSession session);
    List<QuizResponse> findAllBySessionAndStudent(QuizSession session, User student);
    long countBySessionAndQuestionAndSelectedOptionId(QuizSession session, Question question, Long selectedOptionId);
    void deleteAllBySession(QuizSession session);
}

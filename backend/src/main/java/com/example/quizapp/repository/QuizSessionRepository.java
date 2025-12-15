package com.example.quizapp.repository;

import com.example.quizapp.entity.QuizSession;
import com.example.quizapp.entity.User;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    Optional<QuizSession> findByAccessCode(String accessCode);
    Set<QuizSession> findAllByCreatedBy(User user);
}

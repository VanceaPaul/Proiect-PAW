package com.example.quizapp.repository;

import com.example.quizapp.entity.AnswerOption;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
    Optional<AnswerOption> findByIdAndQuestionId(Long id, Long questionId);
}

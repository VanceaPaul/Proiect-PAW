package com.example.quizapp.repository;

import com.example.quizapp.entity.Question;
import com.example.quizapp.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByCreatedBy(User user);
}

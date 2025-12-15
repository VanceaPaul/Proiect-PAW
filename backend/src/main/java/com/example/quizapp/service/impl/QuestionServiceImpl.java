package com.example.quizapp.service.impl;

import com.example.quizapp.dto.AnswerOptionDto;
import com.example.quizapp.dto.QuestionRequest;
import com.example.quizapp.dto.QuestionResponse;
import com.example.quizapp.entity.AnswerOption;
import com.example.quizapp.entity.Question;
import com.example.quizapp.entity.User;
import com.example.quizapp.entity.UserRole;
import com.example.quizapp.repository.QuestionRepository;
import com.example.quizapp.repository.UserRepository;
import com.example.quizapp.service.QuestionService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public QuestionServiceImpl(QuestionRepository questionRepository, UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public QuestionResponse createQuestion(QuestionRequest request, String professorEmail) {
        User professor = loadProfessor(professorEmail);
        validateOptions(request.getOptions());
        Question question = new Question();
        question.setText(request.getText());
        question.setCreatedBy(professor);
        Set<AnswerOption> options = request.getOptions().stream()
            .map(this::toEntity)
            .collect(Collectors.toSet());
        question.setOptions(options);
        Question saved = questionRepository.save(question);
        return toResponse(saved);
    }

    @Override
    public QuestionResponse updateQuestion(Long id, QuestionRequest request, String professorEmail) {
        User professor = loadProfessor(professorEmail);
        Question question = getOwnedQuestion(id, professor);
        validateOptions(request.getOptions());
        question.setText(request.getText());
        question.setOptions(request.getOptions().stream()
            .map(this::toEntity)
            .collect(Collectors.toSet()));
        return toResponse(questionRepository.save(question));
    }

    @Override
    public void deleteQuestion(Long id, String professorEmail) {
        User professor = loadProfessor(professorEmail);
        Question question = getOwnedQuestion(id, professor);
        questionRepository.delete(question);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> listQuestions(String professorEmail) {
        User professor = loadProfessor(professorEmail);
        return questionRepository.findAllByCreatedBy(professor).stream()
            .map(this::toResponse)
            .toList();
    }

    private Question getOwnedQuestion(Long id, User professor) {
        Question question = questionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Question not found"));
        if (!question.getCreatedBy().getId().equals(professor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this question");
        }
        return question;
    }

    private User loadProfessor(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (user.getRole() != UserRole.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a professor");
        }
        return user;
    }

    private AnswerOption toEntity(AnswerOptionDto dto) {
        AnswerOption option = new AnswerOption();
        option.setId(dto.getId());
        option.setText(dto.getText());
        option.setCorrect(dto.isCorrect());
        return option;
    }

    private QuestionResponse toResponse(Question question) {
        List<AnswerOptionDto> options = question.getOptions().stream()
            .map(option -> {
                AnswerOptionDto dto = new AnswerOptionDto();
                dto.setId(option.getId());
                dto.setText(option.getText());
                dto.setCorrect(option.isCorrect());
                return dto;
            })
            .toList();
        return new QuestionResponse(question.getId(), question.getText(), options);
    }

    private void validateOptions(List<AnswerOptionDto> options) {
        long correctCount = options.stream().filter(AnswerOptionDto::isCorrect).count();
        if (correctCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one option must be correct");
        }
    }
}

package com.example.quizapp.service.impl;

import com.example.quizapp.dto.AnswerDistributionDto;
import com.example.quizapp.dto.QuestionResponse;
import com.example.quizapp.dto.QuestionScoreDto;
import com.example.quizapp.dto.QuizSessionRequest;
import com.example.quizapp.dto.QuizSessionResponse;
import com.example.quizapp.dto.QuizStatsResponse;
import com.example.quizapp.dto.ScoreResponse;
import com.example.quizapp.dto.SubmitAnswersRequest;
import com.example.quizapp.dto.SubmitAnswerRequest;
import com.example.quizapp.dto.StudentScoreDto;
import com.example.quizapp.entity.AnswerOption;
import com.example.quizapp.entity.Question;
import com.example.quizapp.entity.QuizResponse;
import com.example.quizapp.entity.QuizSession;
import com.example.quizapp.entity.QuizSessionStatus;
import com.example.quizapp.entity.User;
import com.example.quizapp.entity.UserRole;
import com.example.quizapp.repository.AnswerOptionRepository;
import com.example.quizapp.repository.QuestionRepository;
import com.example.quizapp.repository.QuizResponseRepository;
import com.example.quizapp.repository.QuizSessionRepository;
import com.example.quizapp.repository.UserRepository;
import com.example.quizapp.service.QuizSessionService;
import jakarta.persistence.EntityNotFoundException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class QuizSessionServiceImpl implements QuizSessionService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final QuizSessionRepository sessionRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final QuizResponseRepository responseRepository;
    private final AnswerOptionRepository optionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    public QuizSessionServiceImpl(QuizSessionRepository sessionRepository,
                                  QuestionRepository questionRepository,
                                  UserRepository userRepository,
                                  QuizResponseRepository responseRepository,
                                  AnswerOptionRepository optionRepository,
                                  SimpMessagingTemplate messagingTemplate) {
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.responseRepository = responseRepository;
        this.optionRepository = optionRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public QuizSessionResponse createSession(QuizSessionRequest request, String professorEmail) {
        User professor = loadProfessor(professorEmail);
        QuizSession session = new QuizSession();
        session.setTitle(request.getTitle());
        session.setDurationSeconds(request.getDurationSeconds());
        session.setCreatedBy(professor);
        session.setAccessCode(generateCode());
        Set<Question> questions = request.getQuestionIds().stream()
            .map(id -> loadProfessorQuestion(id, professor))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        session.setQuestions(questions);
        return toProfessorResponse(sessionRepository.save(session));
    }

    @Override
    public QuizSessionResponse activateSession(Long sessionId, String professorEmail) {
        User professor = loadProfessor(professorEmail);
        QuizSession session = getOwnedSession(sessionId, professor);
        session.setStatus(QuizSessionStatus.ACTIVE);
        session.setActivatedAt(Instant.now());
        session.setUpdatedAt(Instant.now());
        return toProfessorResponse(sessionRepository.save(session));
    }

    @Override
    public QuizSessionResponse closeSession(Long sessionId, String professorEmail) {
        User professor = loadProfessor(professorEmail);
        QuizSession session = getOwnedSession(sessionId, professor);
        session.setStatus(QuizSessionStatus.CLOSED);
        session.setClosedAt(Instant.now());
        session.setUpdatedAt(Instant.now());
        return toProfessorResponse(sessionRepository.save(session));
    }

    @Override
    public QuizSessionResponse updateSession(Long sessionId, QuizSessionRequest request, String professorEmail) {
        User professor = loadProfessor(professorEmail);
        QuizSession session = getOwnedSession(sessionId, professor);
        session.setTitle(request.getTitle());
        session.setDurationSeconds(request.getDurationSeconds());
        Set<Question> questions = request.getQuestionIds().stream()
            .map(id -> loadProfessorQuestion(id, professor))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        session.setQuestions(questions);
        session.setUpdatedAt(Instant.now());
        return toProfessorResponse(sessionRepository.save(session));
    }

    @Override
    public void deleteSession(Long sessionId, String professorEmail) {
        User professor = loadProfessor(professorEmail);
        QuizSession session = getOwnedSession(sessionId, professor);
        responseRepository.deleteAllBySession(session);
        sessionRepository.delete(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSessionResponse> listSessions(String professorEmail) {
        User professor = loadProfessor(professorEmail);
        return sessionRepository.findAllByCreatedBy(professor).stream()
            .sorted(Comparator.comparing(QuizSession::getCreatedAt).reversed())
            .map(this::toProfessorResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizSessionResponse getSessionForStudent(String accessCode) {
        QuizSession session = sessionRepository.findByAccessCode(accessCode)
            .filter(s -> s.getStatus() == QuizSessionStatus.ACTIVE)
            .orElseThrow(() -> new EntityNotFoundException("Active session not found for code"));
        return toStudentResponse(session);
    }

    @Override
    public QuizStatsResponse submitAnswers(String accessCode, String studentEmail, SubmitAnswersRequest request) {
        QuizSession session = sessionRepository.findByAccessCode(accessCode)
            .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        if (session.getStatus() != QuizSessionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is not active");
        }
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one answer is required");
        }
        User student = loadStudent(studentEmail);
        List<QuizResponse> existingResponses = responseRepository.findAllBySessionAndStudent(session, student);
        for (SubmitAnswerRequest answer : request.getAnswers()) {
            Question question = session.getQuestions().stream()
                .filter(q -> q.getId().equals(answer.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question not part of session"));
            AnswerOption option = optionRepository.findByIdAndQuestionId(answer.getOptionId(), question.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Option not part of question"));
            existingResponses.stream()
                .filter(resp -> resp.getQuestion().getId().equals(question.getId()))
                .forEach(responseRepository::delete);
            QuizResponse response = new QuizResponse();
            response.setSession(session);
            response.setQuestion(question);
            response.setStudent(student);
            response.setSelectedOptionId(option.getId());
            responseRepository.save(response);
            QuizStatsResponse stats = computeStats(session, question);
            broadcastStats(stats);
        }
        // Return stats for last answered question for convenience
        SubmitAnswerRequest lastAnswer = request.getAnswers().get(request.getAnswers().size() - 1);
        return getStats(session.getId(), lastAnswer.getQuestionId(), session.getCreatedBy().getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public ScoreResponse getScore(String accessCode, String studentEmail) {
        QuizSession session = sessionRepository.findByAccessCode(accessCode)
            .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        User student = loadStudent(studentEmail);
        List<QuizResponse> responses = responseRepository.findAllBySessionAndStudent(session, student);
        Map<Long, QuizResponse> responsesByQuestion = responses.stream()
            .collect(Collectors.toMap(resp -> resp.getQuestion().getId(), resp -> resp, (existing, replacement) -> existing));

        int correct = 0;
        List<QuestionScoreDto> details = new ArrayList<>();
        for (Question question : session.getQuestions()) {
            QuizResponse response = responsesByQuestion.get(question.getId());
            Long selectedOptionId = response != null ? response.getSelectedOptionId() : null;
            boolean answered = selectedOptionId != null;
            List<Long> correctOptionIds = question.getOptions().stream()
                .filter(AnswerOption::isCorrect)
                .map(AnswerOption::getId)
                .collect(Collectors.toList());
            boolean isCorrect = answered && correctOptionIds.contains(selectedOptionId);
            if (isCorrect) {
                correct++;
            }
            details.add(new QuestionScoreDto(question.getId(), question.getText(), isCorrect, answered,
                selectedOptionId, correctOptionIds));
        }
        return new ScoreResponse(session.getId(), correct, session.getQuestions().size(), details);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentScoreDto> listScores(Long sessionId, String professorEmail) {
        User professor = loadProfessor(professorEmail);
        QuizSession session = getOwnedSession(sessionId, professor);
        List<QuizResponse> responses = responseRepository.findAllBySession(session);
        Map<User, List<QuizResponse>> responsesByStudent = responses.stream()
            .collect(Collectors.groupingBy(QuizResponse::getStudent));
        int total = session.getQuestions().size();
        return responsesByStudent.entrySet().stream()
            .map(entry -> {
                User student = entry.getKey();
                String studentName = student.getFullName() != null && !student.getFullName().isBlank()
                    ? student.getFullName()
                    : student.getEmail();
                int correct = 0;
                for (QuizResponse response : entry.getValue()) {
                    Question question = response.getQuestion();
                    boolean isCorrect = question.getOptions().stream()
                        .anyMatch(option -> option.getId().equals(response.getSelectedOptionId()) && option.isCorrect());
                    if (isCorrect) {
                        correct++;
                    }
                }
                return new StudentScoreDto(student.getId(),
                    studentName,
                    student.getEmail(),
                    correct,
                    total,
                    true);
            })
            .sorted(Comparator.comparingInt(StudentScoreDto::getCorrect).reversed()
                .thenComparing(StudentScoreDto::getStudentName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizStatsResponse getStats(Long sessionId, Long questionId, String professorEmail) {
        QuizSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        if (!session.getCreatedBy().getEmail().equalsIgnoreCase(professorEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Session not owned by professor");
        }
        Question question = session.getQuestions().stream()
            .filter(q -> q.getId().equals(questionId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Question not found in session"));
        return computeStats(session, question);
    }

    private QuizStatsResponse computeStats(QuizSession session, Question question) {
        List<AnswerDistributionDto> distribution = question.getOptions().stream()
            .map(option -> new AnswerDistributionDto(option.getId(),
                responseRepository.countBySessionAndQuestionAndSelectedOptionId(session, question, option.getId())))
            .toList();
        long total = distribution.stream().mapToLong(AnswerDistributionDto::getCount).sum();
        return new QuizStatsResponse(session.getId(), question.getId(), total, distribution);
    }

    private void broadcastStats(QuizStatsResponse stats) {
        String destination = "/topic/sessions/" + stats.getSessionId() + "/questions/" + stats.getQuestionId();
        messagingTemplate.convertAndSend(destination, stats);
    }

    private User loadProfessor(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (user.getRole() != UserRole.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a professor");
        }
        return user;
    }

    private User loadStudent(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (user.getRole() != UserRole.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a student");
        }
        return user;
    }

    private Question loadProfessorQuestion(Long id, User professor) {
        Question question = questionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Question not found"));
        if (!question.getCreatedBy().getId().equals(professor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Question does not belong to professor");
        }
        return question;
    }

    private QuizSession getOwnedSession(Long id, User professor) {
        QuizSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        if (!session.getCreatedBy().getId().equals(professor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Session not owned by professor");
        }
        return session;
    }

    private String generateCode() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            builder.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return builder.toString();
    }

    private QuizSessionResponse toProfessorResponse(QuizSession session) {
        return toResponse(session, true, false);
    }

    private QuizSessionResponse toStudentResponse(QuizSession session) {
        return toResponse(session, false, true);
    }

    private QuizSessionResponse toResponse(QuizSession session, boolean includeCorrectAnswers, boolean shuffle) {
        QuizSessionResponse response = new QuizSessionResponse();
        response.setId(session.getId());
        response.setTitle(session.getTitle());
        response.setAccessCode(session.getAccessCode());
        response.setStatus(session.getStatus());
        response.setActivatedAt(session.getActivatedAt());
        response.setClosedAt(session.getClosedAt());
        response.setDurationSeconds(session.getDurationSeconds());
        List<Question> questionList = new ArrayList<>(session.getQuestions());
        if (shuffle) {
            Collections.shuffle(questionList, RANDOM);
        }
        List<QuestionResponse> questions = questionList.stream()
            .map(question -> toQuestionResponse(question, includeCorrectAnswers, shuffle))
            .toList();
        response.setQuestions(questions);
        return response;
    }

    private QuestionResponse toQuestionResponse(Question question, boolean includeCorrectAnswers, boolean shuffleOptions) {
        QuestionResponse dto = new QuestionResponse();
        dto.setId(question.getId());
        dto.setText(question.getText());
        List<AnswerOption> optionList = new ArrayList<>(question.getOptions());
        if (shuffleOptions) {
            Collections.shuffle(optionList, RANDOM);
        }
        dto.setOptions(optionList.stream()
            .map(option -> {
                com.example.quizapp.dto.AnswerOptionDto optionDto = new com.example.quizapp.dto.AnswerOptionDto();
                optionDto.setId(option.getId());
                optionDto.setText(option.getText());
                optionDto.setCorrect(includeCorrectAnswers && option.isCorrect());
                return optionDto;
            })
            .toList());
        return dto;
    }
}

package com.ide.project.domain.files.service;

import com.ide.project.domain.files.dto.*;
import com.ide.project.domain.files.entity.Problem;
import com.ide.project.domain.files.entity.ProblemBank;
import com.ide.project.domain.files.entity.Submission;
import com.ide.project.domain.files.entity.TestCase;
import com.ide.project.domain.files.repository.ProblemBankRepository;
import com.ide.project.domain.files.repository.ProblemRepository;
import com.ide.project.domain.files.repository.SubmissionRepository;
import com.ide.project.domain.files.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final ProblemRepository problemRepository;
    private final ProblemBankRepository problemBankRepository;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionRepository submissionRepository;

    // ==========================================
    // 문제 관리 비즈니스 로직
    // ==========================================

    @Override
    @Transactional
    public ProblemResponse createProblem(ProblemCreateRequest request) {
        Problem problem = Problem.builder()
                .spaceId(request.spaceId())
                .createdBy(request.createdBy())
                .problemBankId(request.problemBankId())
                .title(request.title())
                .description(request.description())
                .difficulty(request.difficulty())
                .language(request.language())
                .starterCode(request.starterCode())
                .isPublished(request.isPublished())
                .build();

        return ProblemResponse.from(problemRepository.save(problem));
    }

    @Override
    @Transactional
    public ProblemResponse updateProblem(Long problemId, ProblemUpdateRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없습니다."));

        problem.updateDetails(
                request.title(),
                request.description(),
                request.difficulty(),
                request.language(),
                request.starterCode(),
                request.isPublished()
        );

        return ProblemResponse.from(problem);
    }

    @Override
    @Transactional
    public void deleteProblem(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없습니다."));

        // 문제에 연관된 테스트케이스 먼저 삭제
        testCaseRepository.deleteAllByProblemId(problemId);

        problemRepository.delete(problem);
    }

    @Override
    @Transactional
    public ProblemResponse assignProblemFromBank(ProblemAssignRequest request) {
        ProblemBank bankProblem = problemBankRepository.findById(request.problemBankId())
                .orElseThrow(() -> new IllegalArgumentException("해당 문제 은행 원본을 찾을 수 없습니다."));

        Problem problem = Problem.builder()
                .spaceId(request.spaceId())
                .createdBy(request.createdBy())
                .problemBankId(bankProblem.getId())
                .title(bankProblem.getTitle())
                .description(bankProblem.getDescription())
                .difficulty(bankProblem.getDifficulty())
                .language(bankProblem.getLanguage())
                .starterCode(bankProblem.getStarterCode())
                .isPublished(false)
                .build();

        return ProblemResponse.from(problemRepository.save(problem));
    }

    @Override
    @Transactional(readOnly = true)
    public ProblemResponse getProblemDetails(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없습니다."));
        return ProblemResponse.from(problem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProblemResponse> getProblemsBySpace(Long spaceId) {
        return problemRepository.findAllBySpaceId(spaceId)
                .stream()
                .map(ProblemResponse::from)
                .collect(Collectors.toList());
    }

    // ==========================================
    // 테스트케이스 비즈니스 로직
    // ==========================================

    @Override
    @Transactional
    public void saveTestCases(Long problemId, List<TestCaseCreateRequest> testCaseRequests) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

        // 기존 테스트케이스 먼저 삭제 (중복 누적 방지)
        testCaseRepository.deleteAllByProblemId(problemId);

        List<TestCase> testCases = testCaseRequests.stream()
                .map(req -> TestCase.builder()
                        .problem(problem)
                        .input(req.input())
                        .expectedOutput(req.expectedOutput())
                        .isHidden(req.isHidden())
                        .orderNum(req.orderNum())
                        .build())
                .collect(Collectors.toList());

        testCaseRepository.saveAll(testCases);
    }

    // ==========================================
    // 제출 관리 비즈니스 로직
    // ==========================================

    @Override
    @Transactional
    public void submitCode(SubmissionRequest request) {
        Submission submission = submissionRepository.findByProblemIdAndUserId(request.problemId(), request.userId())
                .orElseGet(() -> Submission.builder()
                        .problemId(request.problemId())
                        .userId(request.userId())
                        .build());

        String status = request.isFinalSubmit() ? "PENDING" : submission.getStatus();

        submission.updateSubmission(request.savedCode(), request.submittedCode(), status);

        submissionRepository.save(submission);

        // TODO: isFinalSubmit == true 라면 채점 서버로 메시지 전송 로직 추가
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(Long problemId, Long userId) {
        Submission submission = submissionRepository.findByProblemIdAndUserId(problemId, userId)
                .orElseThrow(() -> new IllegalArgumentException("제출 정보를 찾을 수 없습니다."));
        return SubmissionResponse.from(submission);
    }

    @Override
    @Transactional
    public void cancelSubmission(Long problemId, Long userId) {
        Submission submission = submissionRepository.findByProblemIdAndUserId(problemId, userId)
                .orElseThrow(() -> new IllegalArgumentException("제출 정보를 찾을 수 없습니다."));

        submission.cancelSubmission();
    }

    @Override
    @Transactional
    public void updateSavedCode(Long problemId, Long userId, CodeUpdateRequest request) {
        Submission submission = submissionRepository.findByProblemIdAndUserId(problemId, userId)
                .orElseThrow(() -> new IllegalArgumentException("제출 정보를 찾을 수 없습니다."));

        submission.updateSavedCode(request.savedCode());
    }
}
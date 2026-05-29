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

    /**
     * 문제 직접 생성
     */
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

    /**
     * 문제 수정
     */
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

    /**
     * 문제 삭제
     */
    @Override
    @Transactional
    public void deleteProblem(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없습니다."));

        testCaseRepository.deleteAllByProblemId(problemId);
        problemRepository.delete(problem);
    }

    /**
     * 문제 은행에서 문제 배정
     * problem_bank_testcases → testcases 테이블로 테스트케이스 복사
     */
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

        Problem savedProblem = problemRepository.save(problem);

        // problem_bank_testcases → testcases 복사
        List<TestCase> bankTestCases = testCaseRepository
                .findAllByProblemBankId(bankProblem.getId());

        if (!bankTestCases.isEmpty()) {
            List<TestCase> problemTestCases = bankTestCases.stream()
                    .map(tc -> TestCase.builder()
                            .problemId(savedProblem.getId())
                            .problemBankId(bankProblem.getId())
                            .input(tc.getInput())
                            .expectedOutput(tc.getExpectedOutput())
                            .isHidden(tc.isHidden())
                            .build())
                    .collect(Collectors.toList());

            testCaseRepository.saveAll(problemTestCases);
        }

        return ProblemResponse.from(savedProblem);
    }

    /**
     * 문제 상세 조회
     */
    @Override
    @Transactional(readOnly = true)
    public ProblemResponse getProblemDetails(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없습니다."));
        return ProblemResponse.from(problem);
    }

    /**
     * 워크스페이스 문제 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProblemResponse> getProblemsBySpace(Long spaceId) {
        return problemRepository.findAllBySpaceId(spaceId)
                .stream()
                .map(ProblemResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 테스트케이스 저장
     */
    @Override
    @Transactional
    public void saveTestCases(Long problemId, List<TestCaseCreateRequest> testCaseRequests) {
        problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

        testCaseRepository.deleteAllByProblemId(problemId);

        List<TestCase> testCases = testCaseRequests.stream()
                .map(req -> TestCase.builder()
                        .problemId(problemId)
                        .input(req.inputCase())
                        .expectedOutput(req.outputCase())
                        .isHidden(req.isExample())
                        .build())
                .collect(Collectors.toList());

        testCaseRepository.saveAll(testCases);
    }

    /**
     * 테스트케이스 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<TestCaseResponse> getTestCases(Long problemId) {
        return testCaseRepository.findAllByProblemId(problemId)
                .stream()
                .map(TestCaseResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 코드 제출 / 임시 저장
     */
    @Override
    @Transactional
    public void submitCode(SubmissionRequest request) {
        Submission submission = submissionRepository
                .findByProblemIdAndUserId(request.problemId(), request.studentId())
                .orElseGet(() -> Submission.builder()
                        .problemId(request.problemId())
                        .userId(request.studentId())
                        .userIdRef(request.studentId())
                        .build());

        String status = request.isFinalSubmit() ? "PENDING" : submission.getStatus();
        submission.updateSubmission(request.savedCode(), request.submittedCode(), status);
        submissionRepository.save(submission);
    }

    /**
     * 제출 정보 조회
     */
    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(Long problemId, Long userId) {
        Submission submission = submissionRepository.findByProblemIdAndUserId(problemId, userId)
                .orElseThrow(() -> new IllegalArgumentException("제출 정보를 찾을 수 없습니다."));
        return SubmissionResponse.from(submission);
    }

    /**
     * 제출 취소
     */
    @Override
    @Transactional
    public void cancelSubmission(Long problemId, Long userId) {
        Submission submission = submissionRepository.findByProblemIdAndUserId(problemId, userId)
                .orElseThrow(() -> new IllegalArgumentException("제출 정보를 찾을 수 없습니다."));
        submission.cancelSubmission();
    }

    /**
     * 임시 저장 코드 수정
     */
    @Override
    @Transactional
    public void updateSavedCode(Long problemId, Long userId, CodeUpdateRequest request) {
        Submission submission = submissionRepository.findByProblemIdAndUserId(problemId, userId)
                .orElseThrow(() -> new IllegalArgumentException("제출 정보를 찾을 수 없습니다."));
        submission.updateSavedCode(request.savedCode());
    }
}
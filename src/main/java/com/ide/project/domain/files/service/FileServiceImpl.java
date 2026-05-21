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

    // 문제 관리 비즈니스 로직

    @Override
    @Transactional
    public ProblemResponse createProblem(ProblemCreateRequest request) {
        // record는 getter 대신 필드명() 형태로 호출합니다.
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

        // Entity의 비즈니스 메서드를 호출하여 Dirty Checking으로 업데이트 수행
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
    public ProblemResponse assignProblemFromBank(ProblemAssignRequest request) {
        // 원본 문제 은행 데이터 조회
        ProblemBank bankProblem = problemBankRepository.findById(request.problemBankId())
                .orElseThrow(() -> new IllegalArgumentException("해당 문제 은행 원본을 찾을 수 없습니다."));

        // 내 워크스페이스 문제로 복사
        Problem problem = Problem.builder()
                .spaceId(request.spaceId())
                .createdBy(request.createdBy())
                .problemBankId(bankProblem.getId())
                .title(bankProblem.getTitle())
                .description(bankProblem.getDescription())
                .difficulty(bankProblem.getDifficulty())
                .language(bankProblem.getLanguage())
                .starterCode(bankProblem.getStarterCode())
                .isPublished(false) // 복사 직후에는 비공개 상태로 설정
                .build();

        return ProblemResponse.from(problemRepository.save(problem));
    }

    @Override
    @Transactional(readOnly = true) // 단순 조회용이므로 성능 최적화를 위해 readOnly 적용
    public ProblemResponse getProblemDetails(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없습니다."));
        return ProblemResponse.from(problem);
    }

    // 테스트케이스 비즈니스 로직

    @Override
    @Transactional
    public void saveTestCases(Long problemId, List<TestCaseCreateRequest> testCaseRequests) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

        List<TestCase> testCases = testCaseRequests.stream()
                .map(req -> {
                    TestCase testCase = new TestCase();
                    testCase.setProblem(problem);
                    testCase.setInput(req.input());
                    testCase.setExpectedOutput(req.expectedOutput());
                    testCase.setHidden(req.isHidden());
                    testCase.setOrderNum(req.orderNum());
                    return testCase;
                })
                .collect(Collectors.toList());

        testCaseRepository.saveAll(testCases);
    }

    // 제출 기록 비즈니스 로직

    @Override
    @Transactional
    public void submitCode(SubmissionRequest request) {
        // 기존에 제출/임시저장한 기록이 있는지 복합키(problemId, userId)로 확인
        Submission submission = submissionRepository.findByProblemIdAndUserId(request.problemId(), request.userId())
                .orElseGet(() -> Submission.builder()
                        .problemId(request.problemId())
                        .userId(request.userId())
                        .build());

        // 최종 제출이면 "PENDING(채점 대기)"으로, 단순 저장이면 기존 상태 유지
        String status = request.isFinalSubmit() ? "PENDING" : submission.getStatus();
        
        // Entity 비즈니스 메서드를 통해 값 갱신
        submission.updateSubmission(request.savedCode(), request.submittedCode(), status);
        
        submissionRepository.save(submission);
        
        // TODO: 만약 request.isFinalSubmit() == true 라면, 여기서 채점 서버로 메시지를 쏘는 로직을 추가하시면 됩니다.
    }
}
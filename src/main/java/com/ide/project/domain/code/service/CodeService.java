package com.ide.project.domain.code.service;

import com.ide.project.domain.code.dto.*;
import com.ide.project.domain.code.executor.CodeExecutor;
import com.ide.project.domain.files.entity.Submission;
import com.ide.project.domain.files.entity.TestCase;
import com.ide.project.domain.files.repository.SubmissionRepository;
import com.ide.project.domain.files.repository.TestCaseRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeService {

    private final CodeExecutor codeExecutor;
    private final SubmissionRepository submissionRepository;
    private final TestCaseRepository testCaseRepository;

    /**
     * 코드 채점
     * 테스트케이스별 코드 실행 및 결과 비교
     * 채점 결과를 Submission에 업데이트 (PASS/FAIL)
     */
    @Transactional
    public GradeResponse grade(GradeRequest request, Long userId) {

        // Submission 가져오기 (없으면 새로 생성)
        Submission submission = submissionRepository
            .findByProblemIdAndUserId(request.problemId(), userId)
            .orElse(Submission.builder()
                .problemId(request.problemId())
                .userId(userId)
                .userIdRef(userId)
                .build());

        // 테스트케이스 가져오기
        List<TestCase> testCases = testCaseRepository
            .findAllByProblemId(request.problemId());

        // 테스트케이스별 실행 및 비교
        List<GradeResponse.TestCaseResult> results = testCases.stream()
            .map(tc -> {
                String actualOutput = codeExecutor.execute(
                    request.language(),
                    request.code(),
                    tc.getInput()
                ).trim();

                boolean passed = actualOutput.equals(tc.getExpectedOutput().trim());

                return new GradeResponse.TestCaseResult(
                    passed,
                    tc.isHidden() ? "hidden" : tc.getInput(),
                    tc.isHidden() ? "hidden" : tc.getExpectedOutput(),
                    tc.isHidden() ? "hidden" : actualOutput
                );
            })
            .collect(Collectors.toList());

        // 채점 결과 계산
        int passCount = (int) results.stream()
            .filter(GradeResponse.TestCaseResult::passed).count();
        int totalCount = results.size();
        String status = passCount == totalCount ? "PASS" : "FAIL";

        // Submission 업데이트
        submission.updateSubmission(null, request.code(), status);
        submissionRepository.save(submission);

        return new GradeResponse(status, passCount, totalCount, results);
    }
}
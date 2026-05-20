package com.ide.project.domain.files.service;

import com.ide.project.domain.files.dto.*;
import com.ide.project.domain.files.entity.*;
import com.ide.project.domain.files.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileServiceImpl implements FileService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    @Transactional
    public void submitCode(Long problemId, SubmissionUpdateRequest request) {
        Long currentStudentId = 1L; // 추후 인증 객체에서 가져올 값

        Optional<Submission> existingSubmission = 
                submissionRepository.findByStudentIdAndProblemId(currentStudentId, problemId);

        if (existingSubmission.isPresent()) {
            existingSubmission.get().updateCode(request.getSubmittedCode());
        } else {
            Submission newSubmission = Submission.builder()
                    .problemId(problemId)
                    .studentId(currentStudentId)
                    .submittedCode(request.getSubmittedCode())
                    .status("PENDING")
                    .build();
            submissionRepository.save(newSubmission);
        }
    }

    @Override
    @Transactional
    public void resetSubmissionCode(Long problemId) {
        Long currentStudentId = 1L;

        submissionRepository.findByStudentIdAndProblemId(currentStudentId, problemId)
                .ifPresent(submissionRepository::delete);

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다: " + problemId));
        
        problem.updateStarterCode(problem.getStarterCode()); 
    }

    // 나머지 메서드들은 기존 구현대로 유지...
    @Override public Long assignProblemToSpace(ProblemAssignRequest request) { return null; }
    @Override public Long createAndAssignProblem(ProblemCreateRequest request) { return null; }
    @Override public ProblemResponse getProblemDetails(Long problemId) { return null; }
    @Override public void updateProblemCode(Long problemId, CodeUpdateRequest request) {}
    @Override public void updateProblem(Long problemId, ProblemUpdateRequest request) {}
    @Override public void deleteProblem(Long problemId) {}
    @Override public Long addTestCase(Long problemId, TestCaseCreateRequest request) { return null; }
    @Override public void deleteTestCase(Long testCaseId) {}
}
package com.ide.project.domain.files.service;

import com.ide.project.domain.files.dto.*;
import com.ide.project.domain.files.entity.Problem;
import com.ide.project.domain.files.entity.Submission;
import com.ide.project.domain.files.repository.ProblemRepository;
import com.ide.project.domain.files.repository.SubmissionRepository;
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
    public Long assignProblemToSpace(ProblemAssignRequest request) {
        return null;
    }

    @Override
    @Transactional
    public Long createAndAssignProblem(ProblemCreateRequest request) {
        Problem problem = Problem.builder()
                .spaceId(request.getSpaceId())
                .creatorId(request.getCreatorId())
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .starterCode(request.getStarterCode())
                .build();

        Problem savedProblem = problemRepository.save(problem);
        return savedProblem.getId();
    }

    @Override
    public ProblemResponse getProblemDetails(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 문제를 찾을 수 없습니다: " + problemId));
        
        return new ProblemResponse(
                problem.getId(), 
                problem.getTitle(), 
                problem.getDescription(), 
                problem.getDifficulty(), 
                problem.getStarterCode()
        );
    }

    @Override
    @Transactional
    public void updateProblemCode(Long problemId, CodeUpdateRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다: " + problemId));
        
        problem.updateStarterCode(request.getCurrentCode());
    }

    @Override
    @Transactional
    public void updateProblem(Long problemId, ProblemUpdateRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 문제를 찾을 수 없습니다: " + problemId));
        
        problem.updateDetails(
                request.getTitle(), 
                request.getDescription(), 
                request.getDifficulty(), 
                request.getStarterCode()
        );
    }

    @Override
    @Transactional
    public void deleteProblem(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 문제를 찾을 수 없습니다: " + problemId));
        
        problemRepository.delete(problem);
    }


    @Override
    @Transactional
    public void submitCode(Long problemId, SubmissionUpdateRequest request) {
        Long currentStudentId = 1L; 

        Optional<Submission> existingSubmission = 
                submissionRepository.findByStudentIdAndProblemId(currentStudentId, problemId);

        if (existingSubmission.isPresent()) {
            Submission submission = existingSubmission.get();
            // 💡 수정: getSubmittedCode()로 변경
            submission.updateCode(request.getSubmittedCode());
        } else {
            Submission newSubmission = Submission.builder()
                    .problemId(problemId)
                    .studentId(currentStudentId)
                    // 💡 수정: getSubmittedCode()로 변경
                    .submittedCode(request.getSubmittedCode()) 
                    .status("PENDING")
                    .build();
            submissionRepository.save(newSubmission);
        }
    }

    

    // 학생 권한 최종 제출 취소 및 코드 초기화 로직 구현 완료
    @Override
    @Transactional
    public void resetSubmissionCode(Long problemId) {
        Long currentStudentId = 1L;

        // 1. 제출했던 테이블 기록이 있다면 삭제 처리
        submissionRepository.findByStudentIdAndProblemId(currentStudentId, problemId)
                .ifPresent(submissionRepository::delete);

        // 2. 학생의 현재 코딩창 코드를 최초 원본 문제의 스타터 코드로 원상복구
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다: " + problemId));
        
        // 제출 취소 시 에디터 코드를 최초 원본 코드로 초기화
        problem.updateStarterCode(problem.getStarterCode()); 
    }

    @Override
    @Transactional
    public Long addTestCase(Long problemId, TestCaseCreateRequest request) {
        return null;
    }

    @Override
    @Transactional
    public void deleteTestCase(Long testCaseId) {
    }
}
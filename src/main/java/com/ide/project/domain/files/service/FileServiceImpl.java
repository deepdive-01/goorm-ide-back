package com.ide.project.domain.files.service;

import com.ide.project.domain.files.dto.*;
import com.ide.project.domain.files.entity.Problem;
import com.ide.project.domain.files.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileServiceImpl implements FileService {

    private final ProblemRepository problemRepository;

    @Override
    @Transactional
    public Long assignProblemToSpace(ProblemAssignRequest request) {
        // TODO: 문제 은행 도메인 연동 시 원본 복사 로직 구현
        return null;
    }

    @Override
    @Transactional
    public Long createAndAssignProblem(ProblemCreateRequest request) {
        Problem problem = Problem.builder()
                .spaceId(request.getSpaceId())
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
    public void updateSubmissionCode(Long problemId, SubmissionUpdateRequest request) {
        // TODO: 최종 제출 수정 비즈니스 로직 필요 시 구현
    }

    @Override
    @Transactional
    public void resetSubmissionCode(Long problemId) {
        // TODO: 최종 제출 취소 및 초기화 비즈니스 로직 필요 시 구현
    }

    @Override
    @Transactional
    public Long addTestCase(Long problemId, TestCaseCreateRequest request) {
        // TODO: 테스트케이스 추가 비즈니스 로직 필요 시 구현
        return null;
    }

    @Override
    @Transactional
    public void deleteTestCase(Long testCaseId) {
        // TODO: 테스트케이스 삭제 비즈니스 로직 필요 시 구현
    }
}
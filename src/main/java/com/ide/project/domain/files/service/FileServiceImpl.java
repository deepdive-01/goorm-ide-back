package com.ide.project.domain.files.service;

import com.ide.project.domain.files.dto.CodeUpdateRequest;
import com.ide.project.domain.files.dto.ProblemCreateRequest;
import com.ide.project.domain.files.dto.ProblemResponse;
import com.ide.project.domain.files.entity.Problem;
import com.ide.project.domain.files.entity.Submission;
import com.ide.project.domain.files.repository.ProblemRepository;
import com.ide.project.domain.files.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;

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
        
        Problem savedProblem = problemRepository.save(problem);
        return ProblemResponse.from(savedProblem);
    }

    @Override
    @Transactional
    public void updateCodeSubmission(CodeUpdateRequest request) {
        // 기존 제출 내역이 있는지 유니크 키(problem_id, user_id)로 조회
        Submission submission = submissionRepository.findByProblemIdAndUserId(request.problemId(), request.userId())
                .orElseGet(() -> Submission.builder()
                        .problemId(request.problemId())
                        .userId(request.userId())
                        .build());

        String status = request.isFinalSubmit() ? "PENDING" : submission.getStatus();
        
        submission.updateSubmission(request.savedCode(), request.submittedCode(), status);
        submissionRepository.save(submission);
        
        // TODO: isFinalSubmit이 true일 경우, 채점 서버(혹은 로직)로 메시지 큐 발행 로직 추가
    }

    @Override
    @Transactional(readOnly = true)
    public ProblemResponse getProblemDetails(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없습니다."));
        return ProblemResponse.from(problem);
    }
}
package com.ide.project.domain.files.service;

import com.ide.project.domain.files.dto.SubmissionUpdateRequest;
import com.ide.project.domain.files.entity.Problem;
import com.ide.project.domain.files.entity.Submission;
import com.ide.project.domain.files.repository.ProblemRepository;
import com.ide.project.domain.files.repository.SubmissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 이 설정이 에러를 막아줍니다.
class FileServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    @DisplayName("최종 코드 제출 테스트")
    void submitCode_Success() {
        Long problemId = 1L;
        SubmissionUpdateRequest request = new SubmissionUpdateRequest("public class Main {}");
        
        // 메인 로직과 동일하게 호출되도록 설정
        when(problemRepository.findById(anyLong())).thenReturn(Optional.of(new Problem()));
        when(submissionRepository.findByStudentIdAndProblemId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        fileService.submitCode(problemId, request);

        verify(submissionRepository, times(1)).save(any(Submission.class));
    }

    @Test
    @DisplayName("제출 취소 테스트")
    void resetSubmission_Success() {
        Long problemId = 1L;
        
        when(problemRepository.findById(anyLong())).thenReturn(Optional.of(new Problem()));
        when(submissionRepository.findByStudentIdAndProblemId(anyLong(), anyLong()))
                .thenReturn(Optional.of(new Submission()));

        fileService.resetSubmissionCode(problemId);

        verify(submissionRepository, times(1)).delete(any(Submission.class));
    }
}
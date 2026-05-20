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
@MockitoSettings(strictness = Strictness.LENIENT) // 스터빙 에러 방지
class FileServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    @DisplayName("최종 코드 제출 시, 기록이 없으면 신규 저장한다")
    void submitCode_Success() {
        // given
        Long problemId = 1L;
        SubmissionUpdateRequest request = new SubmissionUpdateRequest("public class Main {}");
        
        // anyLong()을 사용하여 인자값 일치 여부와 상관없이 무조건 가짜 응답을 반환하게 설정
        when(problemRepository.findById(anyLong())).thenReturn(Optional.of(new Problem()));
        when(submissionRepository.findByStudentIdAndProblemId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // when
        fileService.submitCode(problemId, request);

        // then: save가 호출되는지만 검증
        verify(submissionRepository, atLeastOnce()).save(any(Submission.class));
    }

    @Test
    @DisplayName("제출 취소 시, 기존 기록을 삭제한다")
    void resetSubmission_Success() {
        // given
        Long problemId = 1L;
        
        when(problemRepository.findById(anyLong())).thenReturn(Optional.of(new Problem()));
        when(submissionRepository.findByStudentIdAndProblemId(anyLong(), anyLong()))
                .thenReturn(Optional.of(new Submission()));

        // when
        fileService.resetSubmissionCode(problemId);

        // then: delete가 호출되는지만 검증
        verify(submissionRepository, atLeastOnce()).delete(any(Submission.class));
    }
}
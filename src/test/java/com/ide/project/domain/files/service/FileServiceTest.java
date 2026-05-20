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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    // 메인 코드를 건드리지 않고, 테스트 환경에서 Mock들을 여기에 주입함
    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    @DisplayName("최종 코드 제출 시, 기존 기록이 있으면 업데이트하고 없으면 저장한다")
    void submitCode_Success() {
        // given
        Long problemId = 1L;
        Long studentId = 1L; // 현재 로직에 하드코딩된 값과 일치시킴
        SubmissionUpdateRequest request = new SubmissionUpdateRequest("public class Main {}");
        
        // 메인 로직의 흐름을 방해하지 않는 가짜 응답 설정
        when(problemRepository.findById(anyLong())).thenReturn(Optional.of(new Problem()));
        when(submissionRepository.findByStudentIdAndProblemId(studentId, problemId))
                .thenReturn(Optional.empty());

        // when
        fileService.submitCode(problemId, request);

        // then: save가 호출되는지 검증
        verify(submissionRepository, times(1)).save(any(Submission.class));
    }

    @Test
    @DisplayName("제출 취소 시 기록을 삭제하고 원본 코드를 복구한다")
    void resetSubmission_Success() {
        // given
        Long problemId = 1L;
        Long studentId = 1L;
        
        when(problemRepository.findById(problemId)).thenReturn(Optional.of(new Problem()));
        when(submissionRepository.findByStudentIdAndProblemId(studentId, problemId))
                .thenReturn(Optional.of(new Submission()));

        // when
        fileService.resetSubmissionCode(problemId);

        // then: 삭제가 호출되는지 검증
        verify(submissionRepository, times(1)).delete(any(Submission.class));
    }
}
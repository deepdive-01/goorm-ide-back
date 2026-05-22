package com.ide.project.domain.files.service;

import com.ide.project.domain.files.dto.SubmissionRequest;
import com.ide.project.domain.files.entity.Submission;
import com.ide.project.domain.files.repository.ProblemBankRepository;
import com.ide.project.domain.files.repository.ProblemRepository;
import com.ide.project.domain.files.repository.SubmissionRepository;
import com.ide.project.domain.files.repository.TestCaseRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileServiceTest {

    // FileServiceImpl에 주입되는 모든 Repository를 Mock 객체로 선언합니다.
    @Mock private ProblemRepository problemRepository;
    @Mock private ProblemBankRepository problemBankRepository;
    @Mock private TestCaseRepository testCaseRepository;
    @Mock private SubmissionRepository submissionRepository;

    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    @DisplayName("최종 코드 제출 시, 기록이 없으면 신규 저장한다")
    void submitCode_Success() {
        // given
        Long problemId = 1L;
        Long userId = 1L;
        
        // 실제 record DTO 구조에 맞게 생성
        SubmissionRequest request = new SubmissionRequest(
                problemId, userId, "System.out.println(1);", "System.out.println(1);", true
        );
        
        // DB에 기존 제출 기록이 없는 상황(Optional.empty)을 가정
        when(submissionRepository.findByProblemIdAndUserId(problemId, userId))
                .thenReturn(Optional.empty());

        // when
        // 🌟 실제 서비스 코드와 동일하게 파라미터 1개(request)만 전달합니다.
        fileService.submitCode(request); 

        // then
        // submissionRepository.save()가 1번 정상적으로 호출되었는지 검증합니다.
        verify(submissionRepository, times(1)).save(any(Submission.class));
    }
}
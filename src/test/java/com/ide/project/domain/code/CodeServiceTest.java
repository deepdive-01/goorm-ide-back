package com.ide.project.domain.code;

import com.ide.project.domain.code.dto.GradeRequest;
import com.ide.project.domain.code.dto.GradeResponse;
import com.ide.project.domain.code.executor.CodeExecutor;
import com.ide.project.domain.code.service.CodeService;
import com.ide.project.domain.files.entity.Submission;
import com.ide.project.domain.files.entity.TestCase;
import com.ide.project.domain.files.repository.SubmissionRepository;
import com.ide.project.domain.files.repository.TestCaseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeServiceTest {

    @Mock
    private CodeExecutor codeExecutor;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private TestCaseRepository testCaseRepository;

    @InjectMocks
    private CodeService codeService;

    // =====================
    // 채점 PASS 테스트
    // =====================
    @Test
    @DisplayName("채점 PASS - 모든 테스트케이스 통과")
    void 채점_PASS() {
        // given
        GradeRequest request = new GradeRequest(1L, "python", "print(int(input()) * 2)");

        TestCase tc1 = TestCase.builder()
            .input("5").expectedOutput("10").orderNum(1).isHidden(false).build();
        TestCase tc2 = TestCase.builder()
            .input("3").expectedOutput("6").orderNum(2).isHidden(false).build();

        when(codeExecutor.execute(any(), any(), any()))
            .thenReturn("10\n")
            .thenReturn("6\n");

        when(testCaseRepository.findAllByProblemIdOrderByOrderNumAsc(1L))
            .thenReturn(List.of(tc1, tc2));

        when(submissionRepository.findByProblemIdAndUserId(any(), any()))
            .thenReturn(Optional.empty());

        when(submissionRepository.save(any()))
            .thenReturn(Submission.builder().build());

        // when
        GradeResponse response = codeService.grade(request, 1L);

        // then
        assertThat(response.status()).isEqualTo("PASS");
        assertThat(response.passCount()).isEqualTo(2);
        assertThat(response.totalCount()).isEqualTo(2);
    }

    // =====================
    // 채점 FAIL 테스트
    // =====================
    @Test
    @DisplayName("채점 FAIL - 일부 테스트케이스 실패")
    void 채점_FAIL() {
        // given
        GradeRequest request = new GradeRequest(1L, "python", "print('wrong')");

        TestCase tc1 = TestCase.builder()
            .input("5").expectedOutput("10").orderNum(1).isHidden(false).build();

        when(codeExecutor.execute(any(), any(), any()))
            .thenReturn("wrong\n");

        when(testCaseRepository.findAllByProblemIdOrderByOrderNumAsc(1L))
            .thenReturn(List.of(tc1));

        when(submissionRepository.findByProblemIdAndUserId(any(), any()))
            .thenReturn(Optional.empty());

        when(submissionRepository.save(any()))
            .thenReturn(Submission.builder().build());

        // when
        GradeResponse response = codeService.grade(request, 1L);

        // then
        assertThat(response.status()).isEqualTo("FAIL");
        assertThat(response.passCount()).isEqualTo(0);
        assertThat(response.totalCount()).isEqualTo(1);
    }

    // =====================
    // 숨김 테스트케이스 테스트
    // =====================
    @Test
    @DisplayName("숨김 테스트케이스 - hidden으로 표시")
    void 숨김_테스트케이스() {
        // given
        GradeRequest request = new GradeRequest(1L, "python", "print(int(input()) * 2)");

        TestCase tc1 = TestCase.builder()
            .input("5").expectedOutput("10").orderNum(1).isHidden(true).build();

        when(codeExecutor.execute(any(), any(), any()))
            .thenReturn("10\n");

        when(testCaseRepository.findAllByProblemIdOrderByOrderNumAsc(1L))
            .thenReturn(List.of(tc1));

        when(submissionRepository.findByProblemIdAndUserId(any(), any()))
            .thenReturn(Optional.empty());

        when(submissionRepository.save(any()))
            .thenReturn(Submission.builder().build());

        // when
        GradeResponse response = codeService.grade(request, 1L);

        // then
        assertThat(response.results().get(0).input()).isEqualTo("hidden");
        assertThat(response.results().get(0).expectedOutput()).isEqualTo("hidden");
        assertThat(response.results().get(0).actualOutput()).isEqualTo("hidden");
    }
}
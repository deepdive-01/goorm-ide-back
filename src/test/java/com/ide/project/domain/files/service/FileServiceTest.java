package com.ide.project.domain.files.service;

import com.ide.project.domain.files.dto.*;
import com.ide.project.domain.files.entity.Problem;
import com.ide.project.domain.files.entity.ProblemBank;
import com.ide.project.domain.files.entity.ProblemBankTestCase;
import com.ide.project.domain.files.entity.Submission;
import com.ide.project.domain.files.entity.TestCase;
import com.ide.project.domain.files.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileServiceTest {

    @Mock private ProblemRepository problemRepository;
    @Mock private ProblemBankRepository problemBankRepository;
    @Mock private ProblemBankTestCaseRepository problemBankTestCaseRepository;
    @Mock private TestCaseRepository testCaseRepository;
    @Mock private SubmissionRepository submissionRepository;

    @InjectMocks
    private FileServiceImpl fileService;

    // ==========================================
    // 문제 관리 테스트
    // ==========================================

    @Test
    @DisplayName("문제 생성 시 정상적으로 저장된다")
    void createProblem_Success() {
        // given
        ProblemCreateRequest request = new ProblemCreateRequest(
                1L, 1L, null, "제목", "설명", "EASY", "JAVA", "// 시작 코드", false
        );

        Problem savedProblem = Problem.builder()
                .spaceId(request.spaceId())
                .createdBy(request.createdBy())
                .title(request.title())
                .description(request.description())
                .difficulty(request.difficulty())
                .language(request.language())
                .starterCode(request.starterCode())
                .isPublished(request.isPublished())
                .build();

        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);

        // when
        ProblemResponse response = fileService.createProblem(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("제목");
        assertThat(response.difficulty()).isEqualTo("EASY");
        verify(problemRepository, times(1)).save(any(Problem.class));
    }

    @Test
    @DisplayName("문제 수정 시 정상적으로 업데이트된다")
    void updateProblem_Success() {
        // given
        Long problemId = 1L;
        ProblemUpdateRequest request = new ProblemUpdateRequest(
                "수정된 제목", "수정된 설명", "HARD", "PYTHON", "# 시작 코드", true
        );

        Problem problem = Problem.builder()
                .spaceId(1L)
                .createdBy(1L)
                .title("원래 제목")
                .description("원래 설명")
                .difficulty("EASY")
                .language("JAVA")
                .build();

        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));

        // when
        ProblemResponse response = fileService.updateProblem(problemId, request);

        // then
        assertThat(response.title()).isEqualTo("수정된 제목");
        assertThat(response.difficulty()).isEqualTo("HARD");
        assertThat(response.language()).isEqualTo("PYTHON");
    }

    @Test
    @DisplayName("존재하지 않는 문제 수정 시 예외가 발생한다")
    void updateProblem_NotFound() {
        // given
        Long problemId = 999L;
        ProblemUpdateRequest request = new ProblemUpdateRequest(
                "제목", "설명", "EASY", "JAVA", null, false
        );

        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fileService.updateProblem(problemId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 문제를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("문제 삭제 시 테스트케이스도 함께 삭제된다")
    void deleteProblem_Success() {
        // given
        Long problemId = 1L;

        Problem problem = Problem.builder()
                .spaceId(1L)
                .createdBy(1L)
                .title("삭제할 문제")
                .description("설명")
                .difficulty("EASY")
                .language("JAVA")
                .build();

        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));

        // when
        fileService.deleteProblem(problemId);

        // then
        verify(testCaseRepository, times(1)).deleteAllByProblemId(problemId);
        verify(problemRepository, times(1)).delete(problem);
    }

    @Test
    @DisplayName("존재하지 않는 문제 삭제 시 예외가 발생한다")
    void deleteProblem_NotFound() {
        // given
        Long problemId = 999L;
        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fileService.deleteProblem(problemId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 문제를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("문제 은행에서 문제 배정 시 비공개 상태로 복사된다")
    void assignProblemFromBank_Success() {
        // given
        ProblemAssignRequest request = new ProblemAssignRequest(1L, 1L, 1L);

        ProblemBank bankProblem = mock(ProblemBank.class);
        when(bankProblem.getId()).thenReturn(1L);
        when(bankProblem.getTitle()).thenReturn("은행 문제");
        when(bankProblem.getDescription()).thenReturn("설명");
        when(bankProblem.getDifficulty()).thenReturn("EASY");
        when(bankProblem.getLanguage()).thenReturn("JAVA");
        when(bankProblem.getStarterCode()).thenReturn("// 시작");

        Problem savedProblem = Problem.builder()
                .spaceId(1L)
                .createdBy(1L)
                .problemBankId(1L)
                .title("은행 문제")
                .description("설명")
                .difficulty("EASY")
                .language("JAVA")
                .isPublished(false)
                .build();

        ProblemBankTestCase tc = mock(ProblemBankTestCase.class);
        when(tc.getInput()).thenReturn("input");
        when(tc.getExpectedOutput()).thenReturn("output");
        when(tc.isHidden()).thenReturn(false);

        when(problemBankRepository.findById(request.problemBankId())).thenReturn(Optional.of(bankProblem));
        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);
        when(problemBankTestCaseRepository.findAllByProblemBankIdOrderByOrderNumAsc(1L))
            .thenReturn(List.of(tc));

        // when
        ProblemResponse response = fileService.assignProblemFromBank(request);

        // then
        assertThat(response.isPublished()).isFalse();
        assertThat(response.title()).isEqualTo("은행 문제");
        verify(problemRepository, times(1)).save(any(Problem.class));
        verify(testCaseRepository, times(1)).saveAll(any(List.class));
    }

    @Test
    @DisplayName("문제 상세 조회 시 정상적으로 반환된다")
    void getProblemDetails_Success() {
        // given
        Long problemId = 1L;

        Problem problem = Problem.builder()
                .spaceId(1L)
                .createdBy(1L)
                .title("테스트 문제")
                .description("설명")
                .difficulty("EASY")
                .language("JAVA")
                .build();

        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));

        // when
        ProblemResponse response = fileService.getProblemDetails(problemId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("테스트 문제");
    }

    @Test
    @DisplayName("워크스페이스 문제 목록 조회 시 정상적으로 반환된다")
    void getProblemsBySpace_Success() {
        // given
        Long spaceId = 1L;

        List<Problem> problems = List.of(
                Problem.builder().spaceId(spaceId).createdBy(1L).title("문제1").description("설명").difficulty("EASY").language("JAVA").build(),
                Problem.builder().spaceId(spaceId).createdBy(1L).title("문제2").description("설명").difficulty("HARD").language("PYTHON").build()
        );

        when(problemRepository.findAllBySpaceId(spaceId)).thenReturn(problems);

        // when
        List<ProblemResponse> response = fileService.getProblemsBySpace(spaceId);

        // then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).title()).isEqualTo("문제1");
        assertThat(response.get(1).title()).isEqualTo("문제2");
    }

    // ==========================================
    // 테스트케이스 관리 테스트
    // ==========================================

    @Test
    @DisplayName("테스트케이스 저장 시 기존 데이터를 삭제 후 저장한다")
    void saveTestCases_Success() {
        // given
        Long problemId = 1L;

        Problem problem = Problem.builder()
                .spaceId(1L)
                .createdBy(1L)
                .title("문제")
                .description("설명")
                .difficulty("EASY")
                .language("JAVA")
                .build();

        List<TestCaseCreateRequest> requests = List.of(
                new TestCaseCreateRequest("input1", "output1", false),
                new TestCaseCreateRequest("input2", "output2", true)
        );

        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));

        // when
        fileService.saveTestCases(problemId, requests);

        // then
        verify(testCaseRepository, times(1)).deleteAllByProblemId(problemId);
        verify(testCaseRepository, times(1)).saveAll(any(List.class));
    }

    // ==========================================
    // 제출 관리 테스트
    // ==========================================

    @Test
    @DisplayName("최종 코드 제출 시 기록이 없으면 신규 저장한다")
    void submitCode_NewSubmission_Success() {
        // given
        SubmissionRequest request = new SubmissionRequest(
                1L, 1L, "System.out.println(1);", "System.out.println(1);", true
        );

        when(submissionRepository.findByProblemIdAndUserId(1L, 1L))
                .thenReturn(Optional.empty());

        // when
        fileService.submitCode(request);

        // then
        verify(submissionRepository, times(1)).save(any(Submission.class));
    }

    @Test
    @DisplayName("임시저장 시 기존 기록이 있으면 업데이트한다")
    void submitCode_UpdateExisting_Success() {
        // given
        SubmissionRequest request = new SubmissionRequest(
                1L, 1L, "// 임시저장 코드", null, false
        );

        Submission existing = Submission.builder()
                .problemId(1L)
                .userId(1L)
                .userIdRef(1L)
                .build();

        when(submissionRepository.findByProblemIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(existing));

        // when
        fileService.submitCode(request);

        // then
        verify(submissionRepository, times(1)).save(any(Submission.class));
    }

    @Test
    @DisplayName("제출 정보 조회 시 정상적으로 반환된다")
    void getSubmission_Success() {
        // given
        Long problemId = 1L;
        Long userId = 1L;

        Submission submission = Submission.builder()
                .problemId(problemId)
                .userId(userId)
                .userIdRef(userId)
                .savedCode("// 코드")
                .status("DRAFT")
                .build();

        when(submissionRepository.findByProblemIdAndUserId(problemId, userId))
                .thenReturn(Optional.of(submission));

        // when
        SubmissionResponse response = fileService.getSubmission(problemId, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.savedCode()).isEqualTo("// 코드");
    }

    @Test
    @DisplayName("제출 정보가 없을 때 조회 시 예외가 발생한다")
    void getSubmission_NotFound() {
        // given
        when(submissionRepository.findByProblemIdAndUserId(999L, 999L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fileService.getSubmission(999L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제출 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("제출 취소 시 PENDING 상태가 DRAFT로 변경된다")
    void cancelSubmission_Success() {
        // given
        Long problemId = 1L;
        Long userId = 1L;

        Submission submission = Submission.builder()
                .problemId(problemId)
                .userId(userId)
                .userIdRef(userId)
                .submittedCode("// 제출 코드")
                .status("DRAFT")
                .build();

        submission.updateSubmission(null, null, "PENDING");

        when(submissionRepository.findByProblemIdAndUserId(problemId, userId))
                .thenReturn(Optional.of(submission));

        // when
        fileService.cancelSubmission(problemId, userId);

        // then
        assertThat(submission.getStatus()).isEqualTo("DRAFT");
        assertThat(submission.getSubmittedCode()).isNull();
    }

    @Test
    @DisplayName("임시저장 코드 수정 시 정상적으로 업데이트된다")
    void updateSavedCode_Success() {
        // given
        Long problemId = 1L;
        Long userId = 1L;
        CodeUpdateRequest request = new CodeUpdateRequest(1L, 1L, "// 수정된 코드");

        Submission submission = Submission.builder()
                .problemId(problemId)
                .userId(userId)
                .userIdRef(userId)
                .savedCode("// 기존 코드")
                .build();

        when(submissionRepository.findByProblemIdAndUserId(problemId, userId))
                .thenReturn(Optional.of(submission));

        // when
        fileService.updateSavedCode(problemId, userId, request);

        // then
        assertThat(submission.getSavedCode()).isEqualTo("// 수정된 코드");
    }

    @Test
    @DisplayName("임시저장 코드 수정 시 제출 정보가 없으면 예외가 발생한다")
    void updateSavedCode_NotFound() {
        // given
        CodeUpdateRequest request = new CodeUpdateRequest(999L, 999L, "// 코드");

        when(submissionRepository.findByProblemIdAndUserId(999L, 999L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fileService.updateSavedCode(999L, 999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제출 정보를 찾을 수 없습니다.");
    }
    @Test
@DisplayName("학생 전체 제출 현황 조회 시 정상적으로 반환된다")
void getStudentSubmissions_Success() {
    // given
    Long userId = 1L;

    List<Submission> submissions = List.of(
            Submission.builder()
                    .problemId(1L)
                    .userId(userId)
                    .userIdRef(userId)
                    .savedCode("// 코드1")
                    .status("PASS")
                    .build(),
            Submission.builder()
                    .problemId(2L)
                    .userId(userId)
                    .userIdRef(userId)
                    .savedCode("// 코드2")
                    .status("FAIL")
                    .build()
    );

    when(submissionRepository.findByUserId(userId)).thenReturn(submissions);

    // when
    List<StudentSubmissionResponse> response = fileService.getStudentSubmissions(userId);

    // then
    assertThat(response).hasSize(2);
    assertThat(response.get(0).problemId()).isEqualTo(1L);
    assertThat(response.get(0).status()).isEqualTo("PASS");
    assertThat(response.get(1).problemId()).isEqualTo(2L);
    assertThat(response.get(1).status()).isEqualTo("FAIL");
}

@Test
@DisplayName("학생 제출 현황이 없을 때 빈 목록을 반환한다")
void getStudentSubmissions_Empty() {
    // given
    Long userId = 999L;
    when(submissionRepository.findByUserId(userId)).thenReturn(List.of());

    // when
    List<StudentSubmissionResponse> response = fileService.getStudentSubmissions(userId);

    // then
    assertThat(response).isEmpty();
}
}
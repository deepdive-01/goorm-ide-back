package com.ide.project.domain.files.service;

import com.ide.project.domain.files.dto.ProblemResponse;
import com.ide.project.domain.files.entity.Problem;
import com.ide.project.domain.files.repository.ProblemRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @InjectMocks
    private FileService fileService; // 테스트할 진짜 객체

    @Mock
    private ProblemRepository problemRepository; // 주입될 가짜 DB(Mock)

    @Test
    @DisplayName("문제 상세 조회 성공 테스트")
    void getProblemDetails_Success() {
        // given: 가짜 DB가 어떻게 행동할지 설정
        Problem mockProblem = new Problem();
        mockProblem.setTitle("테스트 문제");
        mockProblem.setStarterCode("public class Main {}");
        when(problemRepository.findById(anyLong())).thenReturn(Optional.of(mockProblem));

        // when: 실제 서비스 로직 실행
        ProblemResponse response = fileService.getProblemDetails(1L);

        // then: 결과 검증
        assertNotNull(response);
        assertEquals("테스트 문제", response.getTitle());
        assertEquals("public class Main {}", response.getStarterCode());
    }

    @Test
    @DisplayName("존재하지 않는 문제 조회 시 예외 발생 테스트")
    void getProblemDetails_NotFound_ThrowsException() {
        // given: DB에서 아무것도 찾지 못한 상황을 모의(Mocking)
        when(problemRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then: 404 에러(BusinessException)가 제대로 터지는지 검증
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileService.getProblemDetails(999L);
        });

        assertEquals(ErrorCode.PROBLEM_NOT_FOUND, exception.getErrorCode());
    }
}
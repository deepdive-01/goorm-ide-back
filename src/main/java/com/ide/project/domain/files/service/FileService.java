package com.ide.project.domain.files.service;

import com.ide.project.domain.files.dto.ProblemAssignRequest;
import com.ide.project.domain.files.dto.ProblemCreateRequest;
import com.ide.project.domain.files.dto.ProblemResponse;
import com.ide.project.domain.files.entity.*;
import com.ide.project.domain.files.repository.*;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final ProblemBankRepository problemBankRepository;
    private final SpaceRepository spaceRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    @Transactional
    public Long assignProblemToSpace(ProblemAssignRequest request) {
        ProblemBank bank = problemBankRepository.findById(request.getProblemBankId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 원본 문제가 존재하지 않습니다. ID: " + request.getProblemBankId()));

        Space space = spaceRepository.findById(request.getSpaceId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 스페이스가 존재하지 않습니다. ID: " + request.getSpaceId()));

        Problem newProblem = new Problem();
        newProblem.setSpace(space);
        newProblem.setProblemBank(bank);
        newProblem.setTitle(bank.getTitle());
        newProblem.setDescription(bank.getDescription());
        newProblem.setDifficulty(bank.getDifficulty());
        newProblem.setLanguage(bank.getLanguage());
        newProblem.setStarterCode(bank.getStarterCode());
        newProblem.setPublished(true);

        Problem savedProblem = problemRepository.save(newProblem);

        List<TestCase> bankTestCases = testCaseRepository.findByProblemBankId(bank.getId());
        for (TestCase bankCase : bankTestCases) {
            TestCase newCase = new TestCase();
            newCase.setProblemBank(bank);
            newCase.setProblem(savedProblem);
            newCase.setInputCase(bankCase.getInputCase());
            newCase.setOutputCase(bankCase.getOutputCase());
            newCase.setExample(bankCase.isExample()); 
            
            testCaseRepository.save(newCase);
        }

        return savedProblem.getId();
    }

    @Transactional
    public Long createAndAssignProblem(Long spaceId, ProblemCreateRequest request) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 스페이스가 존재하지 않습니다. ID: " + spaceId));

        Problem customProblem = new Problem();
        customProblem.setSpace(space);
        customProblem.setTitle(request.getTitle());
        customProblem.setDescription(request.getDescription());
        customProblem.setDifficulty(request.getDifficulty());
        customProblem.setLanguage(request.getLanguage());
        customProblem.setStarterCode(request.getStarterCode());
        customProblem.setPublished(true);

        Problem savedProblem = problemRepository.save(customProblem);

        return savedProblem.getId();
    }

    
     // 학생이 에디터 화면에 진입했을 때, 특정 문제의 정보 및 초기 코드를 DTO 바구니에 담아 변환하는 조회 기능
    
    @Transactional(readOnly = true)
    public ProblemResponse getProblemDetails(Long problemId) {
        // 워크스페이스 문제 테이블(problems)에서 엔티티를 찾고, 없으면 예외를 던져 방어
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 문제가 워크스페이스에 존재하지 않습니다. ID: " + problemId));

        // 데이터베이스에서 가져온 순수 엔티티(Entity) 객체를 안전한 응답용 DTO 객체로 변환하여 리턴
        return new ProblemResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getDescription(),
                problem.getDifficulty(),
                problem.getLanguage(),
                problem.getStarterCode()
        );
    }
    
    // 학생이 수정한 소스코드를 워크스페이스 문제 테이블에 반영(저장)하는 비즈니스 로직
    
    @Transactional
    public void updateProblemCode(Long problemId, com.ide.project.domain.files.dto.CodeUpdateRequest request) {
        // 수정할 대상 문제가 워크스페이스에 존재하는지 검증 및 조회합니다.
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 문제가 워크스페이스에 존재하지 않습니다. ID: " + problemId));

        // 엔티티의 필드를 수정 (JPA의 영속성 컨텍스트 덕분에 트랜잭션 종료 시 자동으로 DB에 UPDATE 쿼리가 날아갑니다.)
        problem.setStarterCode(request.getModifiedCode());
    }
    
     // 강사가 워크스페이스에 출제된 문제의 지문 및 스펙을 수정하는 비즈니스 로직
    
    @Transactional
    public void updateProblem(Long problemId, com.ide.project.domain.files.dto.ProblemUpdateRequest request) {
        // 수정할 대상 문제가 존재하는지 검증 및 조회
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 문제가 워크스페이스에 존재하지 않습니다. ID: " + problemId));

        // 강사가 입력한 새로운 데이터로 엔티티의 값을 변경 (Dirty Checking 작동)
        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setDifficulty(request.getDifficulty());
        problem.setLanguage(request.getLanguage());
        problem.setStarterCode(request.getStarterCode());
    }

    //강사가 워크스페이스에 출제된 문제를 영구 삭제하는 비즈니스 로직
    
    @Transactional
    public void deleteProblem(Long problemId) {
        // 삭제할 대상 문제가 존재하는지 먼저 확인
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 문제가 워크스페이스에 존재하지 않습니다. ID: " + problemId));

        // 데이터베이스 레포지토리를 통해 해당 문제를 완전 제거
        // (주의: 엔티티 설계 상의 영속성 전이(Cascade) 설정에 따라 연관된 데이터가 함께 정리됩니다.)
        problemRepository.delete(problem);
    }
    
    //학생이 제출한 소스코드를 보완하여 재제출(수정)하는 비즈니스 로직
    
    @Transactional
    public void updateSubmissionCode(Long problemId, com.ide.project.domain.files.dto.SubmissionUpdateRequest request) {
        // 학생의 워크스페이스 문제를 조회
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 문제가 워크스페이스에 존재하지 않습니다. ID: " + problemId));

        // 코드를 새롭게 변경하여 반영 (Dirty Checking 작동)
        problem.setStarterCode(request.getReSubmittedCode());
    }

    // 학생이 제출한 풀이 이력을 취소하고 최초 문제 상태로 초기화(삭제)하는 비즈니스 로직
    
    @Transactional
    public void resetSubmissionCode(Long problemId) {
        // 초기화할 대상 문제를 조회
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 문제가 워크스페이스에 존재하지 않습니다. ID: " + problemId));

        // 연관된 원본 문제 은행(ProblemBank)에서 최초의 스타터 코드를 추출
        ProblemBank bank = problem.getProblemBank();
        if (bank == null) {
            throw new IllegalStateException("해당 문제는 원본 문제 은행 정보가 없어 초기화할 수 없습니다.");
        }

        // 학생의 코딩창을 최초 원본 스타터 코드로 원상복구(초기화).
        problem.setStarterCode(bank.getStarterCode());
    }
    
    // 강사가 특정 워크스페이스 문제에 새로운 채점용 테스트케이스를 추가하는 비즈니스 로직

    @Transactional
    public Long addTestCase(Long problemId, com.ide.project.domain.files.dto.TestCaseCreateRequest request) {
        // 테스트케이스를 연결할 대상 문제를 조회합니다.
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 문제가 워크스페이스에 존재하지 않습니다. ID: " + problemId));

        // 새로운 TestCase 엔티티를 생성하고 데이터를 바인딩합니다.
        TestCase testCase = new TestCase();
        testCase.setProblem(problem);
        testCase.setProblemBank(problem.getProblemBank()); // 강사 직접 생성 문제일 경우 null 매칭을 수용합니다.
        testCase.setInputCase(request.getInputCase());
        testCase.setOutputCase(request.getOutputCase());
        testCase.setExample(request.isExample()); // boolean 타입 표준 세터 호출

        // test_cases 테이블에 영속화합니다.
        TestCase savedTestCase = testCaseRepository.save(testCase);
        return savedTestCase.getId();
    }

    
    // 강사가 부적절하거나 잘못 입력된 특정 테스트케이스를 영구 삭제하는 비즈니스 로직

    @Transactional
    public void deleteTestCase(Long testCaseId) {
        // 삭제할 테스트케이스의 존재 여부를 검증합니다.
        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        // 데이터베이스 레포지토리를 통해 레코드를 삭제합니다.
        testCaseRepository.delete(testCase);
    }
}
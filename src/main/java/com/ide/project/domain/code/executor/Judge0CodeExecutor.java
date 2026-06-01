package com.ide.project.domain.code.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.Semaphore;

@Component
@RequiredArgsConstructor
public class Judge0CodeExecutor implements CodeExecutor {

    private final WebClient webClient;

    // 동시 실행 제한 (최대 10개)
    private final Semaphore semaphore = new Semaphore(10);

    private static final Map<String, Integer> LANGUAGE_MAP = Map.of(
        "python", 71,
        "java", 62,
        "javascript", 63,
        "cpp", 54
    );

    @Override
    public String execute(String language, String code, String stdin) {

        // 동시 실행 제한 체크
        if (!semaphore.tryAcquire()) {
            return "ERROR: 서버가 혼잡합니다. 잠시 후 다시 시도해주세요.";
        }

        try {
            // 코드 길이 제한 (10KB)
            if (code.length() > 10000) {
                return "ERROR: 코드가 너무 깁니다. (최대 10KB)";
            }

            // 언어 ID 확인
            Integer languageId = LANGUAGE_MAP.get(language.toLowerCase());
            if (languageId == null) {
                return "ERROR: 지원하지 않는 언어입니다.";
            }

            // 코드 Base64 인코딩
            String encodedCode = Base64.getEncoder()
                .encodeToString(code.getBytes(StandardCharsets.UTF_8));
            String encodedStdin = stdin != null
                ? Base64.getEncoder().encodeToString(stdin.getBytes(StandardCharsets.UTF_8))
                : "";

            // Judge0에 코드 제출 + 결과 즉시 반환 (wait=true)
            Map<String, Object> body = Map.of(
                "language_id", languageId,
                "source_code", encodedCode,
                "stdin", encodedStdin,
                "enable_per_process_and_thread_time_limit", false,
                "enable_per_process_and_thread_memory_limit", false
            );

            Map result = webClient.post()
                .uri("/submissions?base64_encoded=true&wait=true")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            // 결과 처리
            Map<String, Object> status = (Map<String, Object>) result.get("status");
            int statusId = (int) status.get("id");

            // 3: 정상 완료
            if (statusId == 3) {
                String stdout = (String) result.get("stdout");
                if (stdout == null) return "";
                return new String(
                    Base64.getDecoder().decode(stdout.trim().replaceAll("\\s", "")),
                    StandardCharsets.UTF_8
                );
            }

            // 6: 컴파일 에러
            if (statusId == 6) {
                String compileOutput = (String) result.get("compile_output");
                if (compileOutput == null) return "ERROR: 컴파일 에러";
                return "ERROR: " + new String(
                    Base64.getDecoder().decode(compileOutput.trim().replaceAll("\\s", "")),
                    StandardCharsets.UTF_8
                );
            }

            // 5: 시간 초과
            if (statusId == 5) {
                return "ERROR: 실행 시간 초과";
            }

            // 나머지 에러
            String stderr = (String) result.get("stderr");
            if (stderr != null) {
                return "ERROR: " + new String(
                    Base64.getDecoder().decode(stderr.trim().replaceAll("\\s", "")),
                    StandardCharsets.UTF_8
                );
            }
            return "ERROR: 실행 실패 (status: " + statusId + ")";

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        } finally {
            semaphore.release();
        }
    }
}
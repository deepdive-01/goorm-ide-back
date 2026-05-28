package com.ide.project.domain.code.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Judge0CodeExecutor implements CodeExecutor {

    private final WebClient webClient;

    private static final Map<String, Integer> LANGUAGE_MAP = Map.of(
        "python", 71,
        "java", 62,
        "javascript", 63,
        "cpp", 54
    );

    @Override
    public String execute(String language, String code, String stdin) {
        try {
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

            // Judge0에 코드 제출 → 토큰 받기
            Map<String, Object> body = Map.of(
                "language_id", languageId,
                "source_code", encodedCode,
                "stdin", encodedStdin,
                "enable_per_process_and_thread_time_limit", true,
                "enable_per_process_and_thread_memory_limit", true
            );

            Map response = webClient.post()
                .uri("/submissions?base64_encoded=true")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            String token = (String) response.get("token");

            // 폴링으로 결과 조회
            return pollResult(token);

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private String pollResult(String token) throws InterruptedException {
        int maxAttempts = 30;  // 최대 30번 조회
        int attempt = 0;

        while (attempt < maxAttempts) {
            Map result = webClient.get()
                .uri("/submissions/" + token + "?base64_encoded=true")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            Map<String, Object> status = (Map<String, Object>) result.get("status");
            int statusId = (int) status.get("id");

            // 1: 대기중, 2: 실행중 → 계속 폴링
            if (statusId == 1 || statusId == 2) {
                Thread.sleep(1000);
                attempt++;
                continue;
            }

            // 3: Accepted (정상 완료)
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

            // 나머지 에러 (7~14)
            String stderr = (String) result.get("stderr");
            if (stderr != null) {
                return "ERROR: " + new String(
                    Base64.getDecoder().decode(stderr.trim().replaceAll("\\s", "")),
                    StandardCharsets.UTF_8
                );
            }
            return "ERROR: 실행 실패 (status: " + statusId + ")";
        }

        return "ERROR: 실행 시간 초과 (폴링 한도 초과)";
    }
}
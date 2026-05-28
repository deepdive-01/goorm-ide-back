package com.ide.project.domain.code;

import com.ide.project.domain.code.executor.Judge0CodeExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // ← 추가
class Judge0CodeExecutorTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private WebClient.ResponseSpec getResponseSpec;

    private Judge0CodeExecutor judge0CodeExecutor;

    @BeforeEach
    void setUp() {
        judge0CodeExecutor = new Judge0CodeExecutor(webClient);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    }

    @Test
    @DisplayName("Python 코드 실행 성공")
    void python_성공() {
        Map<String, Object> resultResponse = new HashMap<>();
        resultResponse.put("stdout", "SGVsbG8gV29ybGQK");
        resultResponse.put("stderr", null);
        resultResponse.put("compile_output", null);
        resultResponse.put("status", Map.of("id", 3, "description", "Accepted"));

        when(requestHeadersSpec.retrieve())
            .thenReturn(responseSpec)
            .thenReturn(getResponseSpec);
        when(responseSpec.bodyToMono(Map.class))
            .thenReturn(Mono.just(Map.of("token", "test-token")));
        when(getResponseSpec.bodyToMono(Map.class))
            .thenReturn(Mono.just(resultResponse));

        String result = judge0CodeExecutor.execute("python", "print('Hello World')", "");
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("컴파일 에러")
    void 컴파일_에러() {
        Map<String, Object> resultResponse = new HashMap<>();
        resultResponse.put("stdout", null);
        resultResponse.put("stderr", null);
        resultResponse.put("compile_output", "Y29tcGlsZSBlcnJvcg==");
        resultResponse.put("status", Map.of("id", 6, "description", "Compilation Error"));

        when(requestHeadersSpec.retrieve())
            .thenReturn(responseSpec)
            .thenReturn(getResponseSpec);
        when(responseSpec.bodyToMono(Map.class))
            .thenReturn(Mono.just(Map.of("token", "test-token")));
        when(getResponseSpec.bodyToMono(Map.class))
            .thenReturn(Mono.just(resultResponse));

        String result = judge0CodeExecutor.execute("java", "System.out.println(\"Hello\");", "");
        assertThat(result).startsWith("ERROR:");
    }

    @Test
    @DisplayName("실행 시간 초과")
    void 실행_시간_초과() {
        Map<String, Object> resultResponse = new HashMap<>();
        resultResponse.put("stdout", null);
        resultResponse.put("stderr", null);
        resultResponse.put("compile_output", null);
        resultResponse.put("status", Map.of("id", 5, "description", "Time Limit Exceeded"));

        when(requestHeadersSpec.retrieve())
            .thenReturn(responseSpec)
            .thenReturn(getResponseSpec);
        when(responseSpec.bodyToMono(Map.class))
            .thenReturn(Mono.just(Map.of("token", "test-token")));
        when(getResponseSpec.bodyToMono(Map.class))
            .thenReturn(Mono.just(resultResponse));

        String result = judge0CodeExecutor.execute("python", "while True: pass", "");
        assertThat(result).startsWith("ERROR:");
    }

    @Test
    @DisplayName("지원하지 않는 언어")
    void 지원하지_않는_언어() {
        String result = judge0CodeExecutor.execute("ruby", "puts 'Hello'", "");
        assertThat(result).startsWith("ERROR:");
    }

    @Test
    @DisplayName("코드 길이 초과")
    void 코드_길이_초과() {
        String longCode = "a".repeat(10001);
        String result = judge0CodeExecutor.execute("python", longCode, "");
        assertThat(result).startsWith("ERROR:");
    }
}
package com.ide.project.domain.code;

import com.ide.project.domain.code.executor.ProcessCodeExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessCodeExecutorTest {

    private ProcessCodeExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new ProcessCodeExecutor();
    }

    // =====================
    // Python 테스트
    // =====================
    @Test
    @DisplayName("Python 코드 실행 성공")
    void python_성공() {
        String result = executor.execute("python", "print('Hello World')", "");
        assertThat(result).contains("Hello World");
    }

    @Test
    @DisplayName("Python 코드 실행 실패 (문법 오류)")
    void python_실패() {
        String result = executor.execute("python", "print('Hello World'", "");
        assertThat(result).startsWith("ERROR:");
    }

    @Test
    @DisplayName("Python stdin 입력값 테스트")
    void python_stdin() {
        String code = "n = int(input())\nprint(n * 2)";
        String result = executor.execute("python", code, "5");
        assertThat(result).contains("10");
    }

    // =====================
    // Java 테스트
    // =====================
    @Test
    @DisplayName("Java 코드 실행 성공")
    void java_성공() {
        String code = "public class Main { public static void main(String[] args) { System.out.println(\"Hello World\"); } }";
        String result = executor.execute("java", code, "");
        assertThat(result).contains("Hello World");
    }

    @Test
    @DisplayName("Java 코드 실행 실패 (문법 오류)")
    void java_실패() {
        String result = executor.execute("java", "System.out.println(\"Hello World\");", "");
        assertThat(result).startsWith("ERROR:");
    }

    // =====================
    // JavaScript 테스트
    // =====================
    @Test
    @DisplayName("JavaScript 코드 실행 성공")
    void javascript_성공() {
        String result = executor.execute("javascript", "console.log('Hello World')", "");
        assertThat(result).contains("Hello World");
    }

    @Test
    @DisplayName("JavaScript 코드 실행 실패 (문법 오류)")
    void javascript_실패() {
        String result = executor.execute("javascript", "console.log('Hello World'", "");
        assertThat(result).startsWith("ERROR:");
    }
}
package com.ide.project.domain.code;

import com.ide.project.domain.code.executor.Judge0CodeExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class Judge0CodeExecutorTest {

    @Autowired
    private Judge0CodeExecutor judge0CodeExecutor;

    // =====================
    // Python 테스트
    // =====================
    @Test
    @DisplayName("Python 코드 실행 성공")
    void python_성공() {
        String result = judge0CodeExecutor.execute("python", "print('Hello World')", "");
        assertThat(result).contains("Hello World");
    }

    @Test
    @DisplayName("Python 코드 실행 실패 (문법 오류)")
    void python_실패() {
        String result = judge0CodeExecutor.execute("python", "print('Hello World'", "");
        assertThat(result).startsWith("ERROR:");
    }

    @Test
    @DisplayName("Python stdin 입력값 테스트")
    void python_stdin() {
        String code = "n = int(input())\nprint(n * 2)";
        String result = judge0CodeExecutor.execute("python", code, "5");
        assertThat(result).contains("10");
    }

    // =====================
    // Java 테스트
    // =====================
    @Test
    @DisplayName("Java 코드 실행 성공")
    void java_성공() {
        String code = "public class Main { public static void main(String[] args) { System.out.println(\"Hello World\"); } }";
        String result = judge0CodeExecutor.execute("java", code, "");
        assertThat(result).contains("Hello World");
    }

    @Test
    @DisplayName("Java 코드 실행 실패 (문법 오류)")
    void java_실패() {
        String result = judge0CodeExecutor.execute("java", "System.out.println(\"Hello World\");", "");
        assertThat(result).startsWith("ERROR:");
    }

    // =====================
    // JavaScript 테스트
    // =====================
    @Test
    @DisplayName("JavaScript 코드 실행 성공")
    void javascript_성공() {
        String result = judge0CodeExecutor.execute("javascript", "console.log('Hello World')", "");
        assertThat(result).contains("Hello World");
    }

    @Test
    @DisplayName("JavaScript 코드 실행 실패 (문법 오류)")
    void javascript_실패() {
        String result = judge0CodeExecutor.execute("javascript", "console.log('Hello World'", "");
        assertThat(result).startsWith("ERROR:");
    }

    // =====================
    // C++ 테스트
    // =====================
    @Test
    @DisplayName("C++ 코드 실행 성공")
    void cpp_성공() {
        String code = "#include<iostream>\nusing namespace std;\nint main(){ cout<<\"Hello World\"; return 0; }";
        String result = judge0CodeExecutor.execute("cpp", code, "");
        assertThat(result).contains("Hello World");
    }

    @Test
    @DisplayName("C++ 코드 실행 실패 (문법 오류)")
    void cpp_실패() {
        String result = judge0CodeExecutor.execute("cpp", "cout<<\"Hello World\";", "");
        assertThat(result).startsWith("ERROR:");
    }

    // =====================
    // 공통 테스트
    // =====================
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
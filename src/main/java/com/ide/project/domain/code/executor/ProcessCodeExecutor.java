package com.ide.project.domain.code.executor;

import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

@Component
public class ProcessCodeExecutor implements CodeExecutor {

    private static final int TIMEOUT_SECONDS = 10;

    @Override
    public String execute(String language, String code, String stdin) {
        try {
            // 임시 폴더 생성
            Path tempDir = Files.createTempDirectory("code_exec_");
            String fileName = getFileName(language);
            Path codeFile = tempDir.resolve(fileName);

            // 임시 파일에 코드 저장
            Files.writeString(codeFile, code);

            // 언어에 맞는 실행 명령어 생성
            ProcessBuilder pb = buildProcess(language, codeFile, tempDir);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // stdin 입력값 전달
            if (stdin != null && !stdin.isEmpty()) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(stdin.getBytes());
                }
            }

            // 타임아웃 10초
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "ERROR: 실행 시간 초과 (10초)";
            }

            // 실행 결과 반환
            String output = new String(process.getInputStream().readAllBytes());

            // 임시 파일 삭제
            deleteDirectory(tempDir);

            return output;

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private String getFileName(String language) {
        return switch (language.toLowerCase()) {
            case "python"     -> "Main.py";
            case "java"       -> "Main.java";
            case "javascript" -> "Main.js";
            case "cpp"        -> "Main.cpp";
            default -> throw new IllegalArgumentException("지원하지 않는 언어: " + language);
        };
    }

    private ProcessBuilder buildProcess(String language, Path codeFile, Path tempDir) {
        return switch (language.toLowerCase()) {
            case "python"     -> new ProcessBuilder("python3", codeFile.toString());
            case "java"       -> new ProcessBuilder("sh", "-c",
                "javac " + codeFile + " -d " + tempDir + " && java -cp " + tempDir + " Main");
            case "javascript" -> new ProcessBuilder("node", codeFile.toString());
            case "cpp"        -> new ProcessBuilder("sh", "-c",
                "g++ " + codeFile + " -o " + tempDir + "/Main && " + tempDir + "/Main");
            default -> throw new IllegalArgumentException("지원하지 않는 언어: " + language);
        };
    }

    private void deleteDirectory(Path path) throws IOException {
        Files.walk(path)
            .sorted((a, b) -> b.compareTo(a))
            .forEach(p -> {
                try { Files.delete(p); } catch (IOException ignored) {}
            });
    }
}
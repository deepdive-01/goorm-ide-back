package com.ide.project.domain.code.service;

import com.ide.project.domain.code.dto.CodeExecuteRequest;
import com.ide.project.domain.code.dto.CodeExecuteResponse;
import com.ide.project.domain.code.executor.CodeExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CodeService {

    private final CodeExecutor codeExecutor;

    public CodeExecuteResponse execute(CodeExecuteRequest request) {
        String result = codeExecutor.execute(
            request.language(),
            request.code(),
            request.stdin()
        );

        boolean isError = result.startsWith("ERROR:");

        return new CodeExecuteResponse(
            isError ? null : result,  // output
            isError ? result : null,  // stderr
            isError                   // isError
        );
    }
}
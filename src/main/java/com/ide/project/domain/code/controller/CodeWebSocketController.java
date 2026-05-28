package com.ide.project.domain.code.controller;

import com.ide.project.domain.code.dto.CodeExecuteRequest;
import com.ide.project.domain.code.executor.CodeExecutor;
import com.ide.project.global.redis.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CodeWebSocketController {

    private final CodeExecutor codeExecutor;
    private final SimpMessagingTemplate wsTemplate;
    private final RedisPublisher redisPublisher;

    @MessageMapping("/code.{roomId}.execute")
    public void execute(@DestinationVariable String roomId,
                        @Payload CodeExecuteRequest request) {

        //  실행 시작 알림
        wsTemplate.convertAndSend(
            "/topic/code." + roomId + ".result",
            Map.of("status", "RUNNING")
        );

        //  코드 실행
        String result = codeExecutor.execute(
            request.language(),
            request.code(),
            request.stdin()
        );

        boolean isError = result.startsWith("ERROR:");

        //  실행 결과 전송 (이 서버 구독자에게)
        wsTemplate.convertAndSend(
            "/topic/code." + roomId + ".result",
            Map.of(
                "status", isError ? "ERROR" : "SUCCESS",
                "output", isError ? "" : result,
                "stderr", isError ? result : ""
            )
        );

        //  다른 서버 구독자에게도 전송 (Redis Pub/Sub)
        redisPublisher.publish(
            "room:" + roomId + ":events",
            Map.of(
                "type", "EXECUTE_RESULT",
                "status", isError ? "ERROR" : "SUCCESS",
                "output", isError ? "" : result
            ).toString()
        );
    }
}
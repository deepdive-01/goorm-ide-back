package com.ide.project.domain.workspace.controller;

import com.ide.project.domain.workspace.dto.request.EnterRequest;
import com.ide.project.domain.workspace.dto.response.ParticipantEventMessage;
import com.ide.project.domain.workspace.service.WorkspaceSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@RequiredArgsConstructor
public class WorkspaceWebSocketController {

    private final SimpMessagingTemplate wsTemplate;
    private final WorkspaceSessionService workspaceSessionService;

    @MessageMapping("/workspace.{spaceId}.enter")
    public void enter(@DestinationVariable Long spaceId,
                      @Validated @Payload EnterRequest request,
                      SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();

        ParticipantEventMessage message = workspaceSessionService.enter(sessionId, spaceId, request);

        wsTemplate.convertAndSend("/topic/workspace." + spaceId + ".participants", message);
    }

    @MessageMapping("/workspace.{spaceId}.leave")
    public void leave(@DestinationVariable Long spaceId,
                      SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();

        workspaceSessionService.leave(sessionId).ifPresent(message ->
                wsTemplate.convertAndSend("/topic/workspace." + spaceId + ".participants", message)
        );
    }
}

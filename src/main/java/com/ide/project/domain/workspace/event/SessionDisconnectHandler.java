package com.ide.project.domain.workspace.event;

import com.ide.project.domain.workspace.service.WorkspaceSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class SessionDisconnectHandler {

    private final WorkspaceSessionService workspaceSessionService;
    private final SimpMessagingTemplate wsTemplate;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        workspaceSessionService.leave(sessionId).ifPresent(message ->
                wsTemplate.convertAndSend(
                        "/topic/workspace." + message.spaceId() + ".participants",
                        message
                )
        );
    }
}

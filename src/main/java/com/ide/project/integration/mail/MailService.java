package com.ide.project.integration.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    // 발신자 주소
    @Value("${spring.mail.username}")
    private String from;

    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("[codeRun] 이메일 인증 코드입니다.");
        message.setText("인증 코드: " + "[" + code + "]" + "\n5분 이내에 입력해주세요.");

        // 실제 SMTP 서버로 메일을 전송
        mailSender.send(message);

    }

}

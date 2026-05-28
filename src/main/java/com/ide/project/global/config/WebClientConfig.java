package com.ide.project.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {

    @Value("${judge0.base-url}")
    private String judge0BaseUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("X-Auth-Token", "coderun-judge0-secret")
            .baseUrl(judge0BaseUrl)  // ← 환경변수로 관리
            .build();
    }
}
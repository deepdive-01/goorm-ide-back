package com.ide.project.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${judge0.base-url}")
    private String judge0BaseUrl;

    @Bean
    public WebClient webClient() {
        // 타임아웃 설정
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(15));  // 15초 응답 타임아웃

        return WebClient.builder()
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("X-Auth-Token", "coderun-judge0-secret")
            .baseUrl(judge0BaseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
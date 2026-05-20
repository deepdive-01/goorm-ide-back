package com.ide.project.domain.files.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemResponse {
    
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private String starterCode;
}
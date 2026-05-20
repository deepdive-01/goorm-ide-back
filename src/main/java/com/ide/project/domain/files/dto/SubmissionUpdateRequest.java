package com.ide.project.domain.files.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON 역직렬화를 위해 필수
public class SubmissionUpdateRequest {
    
    private String submittedCode; 

}
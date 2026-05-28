package com.ide.project.integration.s3;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    // Spring Cloud AWS가 등록해주는 S3
    private final S3Template s3Template;

    // 버킷 이름
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    // 리전
    @Value("${spring.cloud.aws.region.static}")
    private String region;

    // 업로드 함수
    public String upload(MultipartFile file, String folder) throws IOException {

        // 원본 파일명에서 확장자만 추출
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        // S3에 저장될 고유 경로 + 파일명 생성
        // UUID로 고유한 이름을 만듦
        String key = folder + "/" + UUID.randomUUID() + (extension != null ? "." + extension : "");

        // S3에 업로드
        s3Template.upload(bucket, key, file.getInputStream(), ObjectMetadata.builder().contentType(file.getContentType()).build());

        // 업로드된 파일을 URL로 조합해서 만듬
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    public void delete(String fileUrl) {
        // 전체 URL에서 key값만 추출
        String key = fileUrl.substring(fileUrl.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());

        // S3에서 해당 파일 삭제
        s3Template.deleteObject(bucket, key);

    }


}

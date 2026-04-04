package com.tastyhouse.file.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 파일 저장소 설정 Properties
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

    /**
     * 파일 업로드 물리 경로
     * 예: /Users/god/Downloads/project/github/tastyhouse/upload
     */
    private String uploadPath;

    /**
     * 파일 접근 베이스 URL
     * 로컬: http://localhost:8080/files
     * S3: https://cdn.tastyhouse.com
     */
    private String baseUrl;

    /**
     * 저장소 타입 (local, s3)
     */
    private String type = "local";

    /**
     * S3 버킷 이름
     * 예: tastyhouse-uploads
     */
    private String s3BucketName;

    /**
     * S3 리전
     * 예: ap-northeast-2
     */
    private String s3Region = "ap-northeast-2";

    /**
     * Firebase 서비스 계정 키 파일 경로
     * 예: path/to/serviceAccountKey.json
     */
    private String firebaseServiceAccountPath;

    /**
     * Firebase Storage 버킷 이름
     * 예: PROJECT_ID.firebasestorage.app
     */
    private String firebaseStorageBucket;
}

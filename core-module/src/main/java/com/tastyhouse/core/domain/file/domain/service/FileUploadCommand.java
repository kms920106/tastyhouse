package com.tastyhouse.core.domain.file.domain.service;

/**
 * 파일 업로드 입력 값.
 *
 * <p>과거 core application 계층의 {@code UploadFileCommand}를 도메인 서비스
 * ({@link FileUploadService}) 입력 record로 격하한 것이다.
 */
public record FileUploadCommand(
    String originalFilename,
    byte[] content,
    Long fileSize,
    String contentType
) {

    public static FileUploadCommand of(
        String originalFilename,
        byte[] content,
        Long fileSize,
        String contentType
    ) {
        return new FileUploadCommand(originalFilename, content, fileSize, contentType);
    }
}

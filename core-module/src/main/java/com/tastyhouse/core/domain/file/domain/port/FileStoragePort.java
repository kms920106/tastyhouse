package com.tastyhouse.core.domain.file.domain.port;

/**
 * 파일 스토리지 출력 포트.
 *
 * <p>실제 저장소(S3/Firebase) 어댑터는 external-api의 {@code FileStoragePortAdapter}가 구현한다.
 */
public interface FileStoragePort {

    String store(byte[] content, String storedFilename, String datePath, String contentType);

    String getFileUrl(String filePath);

    void delete(String filePath);
}

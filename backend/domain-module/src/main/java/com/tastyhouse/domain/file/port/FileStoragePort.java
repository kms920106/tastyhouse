package com.tastyhouse.domain.file.port;

/**
 * 파일 스토리지 출력 포트.
 *
 * <p>이 포트는 infrastructure:external(코어)의 {@code FileStoragePortAdapter}가 구현하며,
 * 실제 저장소 전략은 infrastructure:firebase / infrastructure:aws가 제공한다.
 */
public interface FileStoragePort {

    String store(byte[] content, String storedFilename, String datePath, String contentType);

    String getFileUrl(String filePath);

    void delete(String filePath);
}

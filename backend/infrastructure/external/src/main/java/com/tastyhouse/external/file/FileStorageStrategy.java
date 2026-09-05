package com.tastyhouse.external.file;

/**
 * 파일 저장소 전략 인터페이스
 * 로컬 파일 시스템, S3, Firebase 등 다양한 저장소 구현을 지원
 *
 * <p>시그니처는 도메인 포트 {@code FileStoragePort}와 동일한 형태({@code byte[]})다.
 * 코어 external 모듈이 {@code spring-web}(MultipartFile)에 의존하지 않도록 하기 위함이며,
 * {@link FileStoragePortAdapter}는 변환 없이 그대로 위임한다.
 */
public interface FileStorageStrategy {

    /**
     * 파일을 저장소에 저장
     *
     * @param content 저장할 파일의 내용
     * @param storedFilename 저장될 파일명 (UUID + 확장자)
     * @param datePath 날짜 기반 경로 (예: 2025/02/16)
     * @param contentType 파일의 MIME 타입
     * @return 저장된 파일의 상대 경로 (예: 2025/02/16/uuid.jpg)
     */
    String store(byte[] content, String storedFilename, String datePath, String contentType);

    /**
     * 파일의 전체 URL을 반환
     *
     * @param filePath 파일의 상대 경로
     * @return 접근 가능한 전체 URL
     */
    String getFileUrl(String filePath);

    /**
     * 파일 삭제
     *
     * @param filePath 삭제할 파일의 상대 경로
     */
    void delete(String filePath);
}

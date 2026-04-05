package com.tastyhouse.external.file.s3;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.external.file.FileStorageStrategy;
import io.awspring.cloud.s3.S3Operations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Component
@ConditionalOnProperty(name = "file.provider", havingValue = "s3")
@RequiredArgsConstructor
public class S3FileStorage implements FileStorageStrategy {

    private final S3Operations s3Operations;
    private final S3FileStorageProperties properties;

    @Override
    public String store(MultipartFile file, String storedFilename, String datePath) {
        String key = datePath + "/" + storedFilename;
        try {
            s3Operations.upload(properties.getBucketName(), key, file.getInputStream());
            log.info("S3 파일 저장 완료: {}", key);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_STORE_FAILED);
        }
        return key;
    }

    @Override
    public String getFileUrl(String filePath) {
        return properties.getBaseUrl() + "/" + filePath;
    }

    @Override
    public void delete(String filePath) {
        try {
            s3Operations.deleteObject(properties.getBucketName(), filePath);
            log.info("S3 파일 삭제 완료: {}", filePath);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", filePath, e);
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
        }
    }
}

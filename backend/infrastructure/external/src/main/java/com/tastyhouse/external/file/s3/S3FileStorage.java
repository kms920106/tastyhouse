package com.tastyhouse.external.file.s3;

import java.io.IOException;

import io.awspring.cloud.s3.S3Operations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.external.file.FileStorageStrategy;

@Component
@ConditionalOnProperty(name = "file.provider", havingValue = "s3")
public class S3FileStorage implements FileStorageStrategy {

    private static final Logger log = LoggerFactory.getLogger(S3FileStorage.class);

    private final S3Operations s3Operations;
    private final S3FileStorageProperties properties;

    public S3FileStorage(S3Operations s3Operations, S3FileStorageProperties properties) {
        this.s3Operations = s3Operations;
        this.properties = properties;
    }

    @Override
    public String store(MultipartFile file, String storedFilename, String datePath) {
        String key = datePath + "/" + storedFilename;
        try {
            s3Operations.upload(properties.bucketName(), key, file.getInputStream());
            log.info("S3 파일 저장 완료: {}", key);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_STORE_FAILED);
        }
        return key;
    }

    @Override
    public String getFileUrl(String filePath) {
        return properties.baseUrl() + "/" + filePath;
    }

    @Override
    public void delete(String filePath) {
        try {
            s3Operations.deleteObject(properties.bucketName(), filePath);
            log.info("S3 파일 삭제 완료: {}", filePath);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", filePath, e);
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
        }
    }
}

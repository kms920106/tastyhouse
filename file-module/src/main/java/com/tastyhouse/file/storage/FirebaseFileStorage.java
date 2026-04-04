package com.tastyhouse.file.storage;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Firebase Cloud Storage 파일 저장소 구현체
 * file.storage.type=firebase 일 때 활성화
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "file.storage.type", havingValue = "firebase")
@RequiredArgsConstructor
public class FirebaseFileStorage implements FileStorageStrategy {

    private final FileStorageProperties properties;

    @Override
    public String store(MultipartFile file, String storedFilename, String datePath) {
        String key = datePath + "/" + storedFilename;
        try {
            Bucket bucket = StorageClient.getInstance().bucket();
            bucket.create(key, file.getBytes(), file.getContentType());
            log.info("Firebase Storage 파일 저장 완료: {}", key);
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
            Bucket bucket = StorageClient.getInstance().bucket();
            Blob blob = bucket.get(filePath);
            if (blob != null) {
                blob.delete();
            }
            log.info("Firebase Storage 파일 삭제 완료: {}", filePath);
        } catch (Exception e) {
            log.error("Firebase Storage 파일 삭제 실패: {}", filePath, e);
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
        }
    }
}

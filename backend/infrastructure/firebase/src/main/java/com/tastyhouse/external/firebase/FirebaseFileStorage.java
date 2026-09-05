package com.tastyhouse.external.firebase;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.external.file.FileStorageStrategy;

@Component
@ConditionalOnProperty(name = "file.provider", havingValue = "firebase")
public class FirebaseFileStorage implements FileStorageStrategy {

    private static final Logger log = LoggerFactory.getLogger(FirebaseFileStorage.class);

    private final FirebaseStorageProperties properties;

    public FirebaseFileStorage(FirebaseStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String store(byte[] content, String storedFilename, String datePath, String contentType) {
        String key = datePath + "/" + storedFilename;
        Bucket bucket = StorageClient.getInstance().bucket();
        bucket.create(key, content, contentType);
        log.info("Firebase Storage 파일 저장 완료: {}", key);
        return key;
    }

    @Override
    public String getFileUrl(String filePath) {
        String encodedPath = URLEncoder.encode(filePath, StandardCharsets.UTF_8).replace("+", "%20");
        return properties.baseUrl() + "/" + encodedPath + "?alt=media";
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

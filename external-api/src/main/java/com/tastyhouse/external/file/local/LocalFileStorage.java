package com.tastyhouse.external.file.local;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.external.file.FileStorageProperties;
import com.tastyhouse.external.file.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@ConditionalOnProperty(name = "file.storage.type", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
public class LocalFileStorage implements FileStorageStrategy {

    private final FileStorageProperties properties;

    @Override
    public String store(MultipartFile file, String storedFilename, String datePath) {
        Path directoryPath = Paths.get(properties.getUploadPath(), datePath);
        Path filePath = directoryPath.resolve(storedFilename);

        try {
            Files.createDirectories(directoryPath);
            file.transferTo(filePath.toFile());
            log.info("파일 저장 완료: {}", filePath);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_STORE_FAILED);
        }

        return datePath + "/" + storedFilename;
    }

    @Override
    public String getFileUrl(String filePath) {
        return properties.getBaseUrl() + "/" + filePath;
    }

    @Override
    public void delete(String filePath) {
        Path fullPath = Paths.get(properties.getUploadPath(), filePath);
        try {
            Files.deleteIfExists(fullPath);
            log.info("파일 삭제 완료: {}", fullPath);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", fullPath, e);
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
        }
    }
}

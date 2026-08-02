package com.tastyhouse.external.file;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.port.FileStoragePort;

@Component
public class FileStoragePortAdapter implements FileStoragePort {

    private final FileStorageStrategy fileStorageStrategy;

    public FileStoragePortAdapter(FileStorageStrategy fileStorageStrategy) {
        this.fileStorageStrategy = fileStorageStrategy;
    }

    @Override
    public String store(byte[] content, String storedFilename, String datePath, String contentType) {
        MultipartFile file = new ByteArrayMultipartFile(storedFilename, contentType, content);
        return fileStorageStrategy.store(file, storedFilename, datePath);
    }

    @Override
    public String getFileUrl(String filePath) {
        return fileStorageStrategy.getFileUrl(filePath);
    }

    @Override
    public void delete(String filePath) {
        fileStorageStrategy.delete(filePath);
    }
}

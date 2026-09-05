package com.tastyhouse.external.file;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.file.port.FileStoragePort;

@Component
public class FileStoragePortAdapter implements FileStoragePort {

    private final FileStorageStrategy fileStorageStrategy;

    public FileStoragePortAdapter(FileStorageStrategy fileStorageStrategy) {
        this.fileStorageStrategy = fileStorageStrategy;
    }

    @Override
    public String store(byte[] content, String storedFilename, String datePath, String contentType) {
        return fileStorageStrategy.store(content, storedFilename, datePath, contentType);
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

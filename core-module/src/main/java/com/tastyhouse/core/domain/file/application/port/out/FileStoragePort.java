package com.tastyhouse.core.domain.file.application.port.out;

public interface FileStoragePort {

    String store(byte[] content, String storedFilename, String datePath, String contentType);

    String getFileUrl(String filePath);

    void delete(String filePath);
}

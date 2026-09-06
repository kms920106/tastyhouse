package com.tastyhouse.domain.file.port;

public interface FileStoragePort {
    String store(byte[] content, String storedFilename, String datePath, String contentType);

    String getFileUrl(String filePath);

    void delete(String filePath);
}

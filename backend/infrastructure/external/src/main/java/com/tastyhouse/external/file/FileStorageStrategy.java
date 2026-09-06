package com.tastyhouse.external.file;

public interface FileStorageStrategy {

    String store(byte[] content, String storedFilename, String datePath, String contentType);

    String getFileUrl(String filePath);

    void delete(String filePath);
}

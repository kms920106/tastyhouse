package com.tastyhouse.application.file.port.out;

public interface FileStoragePort {
    String store(byte[] content, String storedFilename, String datePath, String contentType);

    String getFileUrl(String filePath);

    FileDeleteResult delete(String filePath);
}

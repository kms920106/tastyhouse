package com.tastyhouse.infrastructure.file.query;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.file.port.FileStoragePort;

@Component
public class FileUrlResolver {
    private final FileStoragePort fileStoragePort;

    public FileUrlResolver(FileStoragePort fileStoragePort) {
        this.fileStoragePort = fileStoragePort;
    }

    public String resolve(String filePath) {
        if (filePath == null) {
            return null;
        }
        return fileStoragePort.getFileUrl(filePath);
    }

    public Map<Long, String> resolveAll(Map<Long, String> filePathById) {
        if (filePathById == null || filePathById.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> urlById = new LinkedHashMap<>();
        filePathById.forEach((id, filePath) -> {
            String url = resolve(filePath);
            if (url != null) {
                urlById.put(id, url);
            }
        });
        return urlById;
    }

    public List<String> resolveAll(Collection<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            return List.of();
        }
        return filePaths.stream()
            .map(this::resolve)
            .filter(Objects::nonNull)
            .toList();
    }
}

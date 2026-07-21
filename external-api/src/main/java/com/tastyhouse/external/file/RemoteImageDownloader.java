package com.tastyhouse.external.file;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.tastyhouse.core.domain.file.application.FileCommandService;
import com.tastyhouse.core.domain.file.application.dto.command.UploadFileCommand;
import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteImageDownloader {

    private final FileCommandService fileCommandService;

    // 외부 URL에서 이미지를 다운로드하여 업로드하고 파일 ID를 반환한다.
    public Long uploadFromUrl(String imageUrl) {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new BusinessException(ErrorCode.FILE_EMPTY);
            }

            byte[] imageBytes = response.body();
            String rawContentType = response.headers().firstValue("Content-Type").orElse("image/jpeg");
            String contentType = rawContentType.split(";")[0].trim();

            String rawFilename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            String filename = rawFilename.contains("?") ? rawFilename.substring(0, rawFilename.indexOf("?")) : rawFilename;

            UploadFileCommand command = UploadFileCommand.of(filename, imageBytes, (long) imageBytes.length, contentType);
            UploadedFileId fileId = fileCommandService.upload(command);
            return fileId.value();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("이미지 다운로드 실패: url={}", imageUrl, e);
            throw new RuntimeException("이미지 다운로드 실패: " + imageUrl, e);
        }
    }
}

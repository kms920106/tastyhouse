package com.tastyhouse.external.file;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tastyhouse.application.crawling.bbq.port.out.RemoteImagePort;
import com.tastyhouse.domain.file.service.FileUploadCommand;
import com.tastyhouse.domain.file.service.FileUploadService;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

@Component
public class RemoteImageDownloader implements RemoteImagePort {

    private static final Logger log = LoggerFactory.getLogger(RemoteImageDownloader.class);

    private final FileUploadService fileUploadService;

    public RemoteImageDownloader(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    // 외부 URL에서 이미지를 다운로드하여 업로드하고 파일 ID를 반환한다.
    @Override
    public Long uploadFromUrl(String imageUrl) {
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
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

            FileUploadCommand command = FileUploadCommand.of(filename, imageBytes, (long) imageBytes.length, contentType);
            UploadedFileId fileId = fileUploadService.upload(command);
            return fileId.value();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("이미지 다운로드 실패: url={}", imageUrl, e);
            throw new RuntimeException("이미지 다운로드 실패: " + imageUrl, e);
        }
    }
}

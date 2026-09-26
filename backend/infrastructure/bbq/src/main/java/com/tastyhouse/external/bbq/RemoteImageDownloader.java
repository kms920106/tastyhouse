package com.tastyhouse.external.bbq;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.tastyhouse.application.crawling.bbq.port.out.RemoteImagePort;
import com.tastyhouse.domain.file.service.FileUploadCommand;
import com.tastyhouse.domain.file.service.FileUploadService;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.restclient.config.HttpRequestFactories;

@Component
public class RemoteImageDownloader implements RemoteImagePort {

    private static final Logger log = LoggerFactory.getLogger(RemoteImageDownloader.class);

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final String DEFAULT_CONTENT_TYPE = "image/jpeg";

    private final RestClient restClient;
    private final FileUploadService fileUploadService;

    public RemoteImageDownloader(RestClient.Builder restClientBuilder, FileUploadService fileUploadService) {
        this.restClient = restClientBuilder
            .requestFactory(HttpRequestFactories.withTimeouts(CONNECT_TIMEOUT, READ_TIMEOUT))
            .build();
        this.fileUploadService = fileUploadService;
    }

    @Override
    public Long uploadFromUrl(String imageUrl) {
        DownloadedImage image = download(imageUrl);

        String rawFilename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        String filename = rawFilename.contains("?") ? rawFilename.substring(0, rawFilename.indexOf("?")) : rawFilename;

        FileUploadCommand command = FileUploadCommand.of(
            filename,
            image.bytes(),
            (long) image.bytes().length,
            image.contentType()
        );
        UploadedFileId fileId = fileUploadService.upload(command);
        return fileId.value();
    }

    private DownloadedImage download(String imageUrl) {
        try {
            return restClient.get()
                .uri(URI.create(imageUrl))
                .exchange((request, response) -> readImage(imageUrl, response));
        } catch (RestClientException e) {
            log.error("이미지 다운로드 실패: url={}", imageUrl, e);
            throw new RuntimeException("이미지 다운로드 실패: " + imageUrl, e);
        }
    }

    private static DownloadedImage readImage(String imageUrl, ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().value() != 200) {
            log.error("이미지 다운로드 응답이 비정상입니다: status={}, url={}", response.getStatusCode().value(), imageUrl);
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        HttpHeaders headers = response.getHeaders();
        if (headers.getContentLength() > MAX_IMAGE_BYTES) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        byte[] bytes;
        try (InputStream body = response.getBody()) {
            bytes = body.readNBytes(MAX_IMAGE_BYTES + 1);
        }
        if (bytes.length > MAX_IMAGE_BYTES) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        if (bytes.length == 0) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }
        return new DownloadedImage(bytes, contentTypeOf(headers));
    }

    private static String contentTypeOf(HttpHeaders headers) {
        String rawContentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
        if (rawContentType == null) {
            return DEFAULT_CONTENT_TYPE;
        }
        return rawContentType.split(";")[0].trim();
    }
}

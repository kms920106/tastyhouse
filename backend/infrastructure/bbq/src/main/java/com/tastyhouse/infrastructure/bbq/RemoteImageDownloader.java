package com.tastyhouse.infrastructure.bbq;

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

import com.tastyhouse.application.crawling.bbq.port.out.DownloadedImage;
import com.tastyhouse.application.crawling.bbq.port.out.ImageDownloadFailure;
import com.tastyhouse.application.crawling.bbq.port.out.ImageDownloadResult;
import com.tastyhouse.application.crawling.bbq.port.out.RemoteImagePort;
import com.tastyhouse.infrastructure.restclient.HttpRequestFactories;

@Component
public class RemoteImageDownloader implements RemoteImagePort {

    private static final Logger log = LoggerFactory.getLogger(RemoteImageDownloader.class);

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final String DEFAULT_CONTENT_TYPE = "image/jpeg";

    private final RestClient restClient;

    public RemoteImageDownloader(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
            .requestFactory(HttpRequestFactories.withTimeouts(CONNECT_TIMEOUT, READ_TIMEOUT))
            .build();
    }

    @Override
    public ImageDownloadResult download(String imageUrl) {
        try {
            return restClient.get()
                .uri(URI.create(imageUrl))
                .exchange((request, response) -> readImage(imageUrl, response));
        } catch (RestClientException e) {
            log.error("이미지 다운로드 실패: url={}", imageUrl, e);
            throw new RuntimeException("이미지 다운로드 실패: " + imageUrl, e);
        }
    }

    private static ImageDownloadResult readImage(String imageUrl, ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().value() != 200) {
            log.error("이미지 다운로드 응답이 비정상입니다: status={}, url={}", response.getStatusCode().value(), imageUrl);
            return ImageDownloadResult.failed(ImageDownloadFailure.EMPTY);
        }

        HttpHeaders headers = response.getHeaders();
        if (headers.getContentLength() > MAX_IMAGE_BYTES) {
            return ImageDownloadResult.failed(ImageDownloadFailure.SIZE_EXCEEDED);
        }

        byte[] bytes;
        try (InputStream body = response.getBody()) {
            bytes = body.readNBytes(MAX_IMAGE_BYTES + 1);
        }
        if (bytes.length > MAX_IMAGE_BYTES) {
            return ImageDownloadResult.failed(ImageDownloadFailure.SIZE_EXCEEDED);
        }
        if (bytes.length == 0) {
            return ImageDownloadResult.failed(ImageDownloadFailure.EMPTY);
        }
        return ImageDownloadResult.downloaded(new DownloadedImage(bytes, contentTypeOf(headers), filenameOf(imageUrl)));
    }

    private static String filenameOf(String imageUrl) {
        String rawFilename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        return rawFilename.contains("?") ? rawFilename.substring(0, rawFilename.indexOf("?")) : rawFilename;
    }

    private static String contentTypeOf(HttpHeaders headers) {
        String rawContentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
        if (rawContentType == null) {
            return DEFAULT_CONTENT_TYPE;
        }
        return rawContentType.split(";")[0].trim();
    }
}

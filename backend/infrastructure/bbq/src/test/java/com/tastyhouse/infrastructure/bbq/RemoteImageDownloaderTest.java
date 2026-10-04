package com.tastyhouse.infrastructure.bbq;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.tastyhouse.application.crawling.bbq.port.out.DownloadedImage;
import com.tastyhouse.application.crawling.bbq.port.out.ImageDownloadFailure;
import com.tastyhouse.application.crawling.bbq.port.out.ImageDownloadResult;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteImageDownloaderTest {

    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final byte[] PNG_BYTES = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};

    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<String> requestedRawPath = new AtomicReference<>();
    private RemoteImageDownloader downloader;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        downloader = new RemoteImageDownloader(RestClient.builder());
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    @DisplayName("퍼센트 인코딩된 URL을 다시 인코딩하지 않고 그대로 요청하며 Content-Type 파라미터와 쿼리를 떼어 돌려준다")
    void keepsPercentEncodedUrlAsIs() {
        server.createContext("/", exchange -> {
            requestedRawPath.set(exchange.getRequestURI().getRawPath());
            respond(exchange, 200, "image/png; charset=binary", PNG_BYTES, PNG_BYTES.length);
        });

        ImageDownloadResult result = downloader.download(baseUrl + "/menu/a%20%EC%B9%98%ED%82%A8.png?v=1");
        assertThat(result.success()).isTrue();
        DownloadedImage image = result.image();

        assertThat(requestedRawPath.get()).isEqualTo("/menu/a%20%EC%B9%98%ED%82%A8.png");
        assertThat(image.bytes()).containsExactly(PNG_BYTES);
        assertThat(image.contentType()).isEqualTo("image/png");
        assertThat(image.filename()).isEqualTo("a%20%EC%B9%98%ED%82%A8.png");
    }

    @Test
    @DisplayName("200이 아닌 응답은 EMPTY 실패 결과를 돌려준다")
    void nonOkStatusIsFileEmpty() {
        server.createContext("/", exchange -> respond(exchange, 404, "text/plain", new byte[0], -1));

        assertThat(downloader.download(baseUrl + "/missing.png").failure())
            .isEqualTo(ImageDownloadFailure.EMPTY);
    }

    @Test
    @DisplayName("Content-Length 없이 상한을 넘겨 흘려보내는 응답도 SIZE_EXCEEDED 실패 결과로 끊는다")
    void streamedBodyOverLimitIsRejected() {
        server.createContext("/", exchange -> respond(exchange, 200, "image/png", new byte[MAX_IMAGE_BYTES + 1], 0));

        assertThat(downloader.download(baseUrl + "/huge.png").failure())
            .isEqualTo(ImageDownloadFailure.SIZE_EXCEEDED);
    }

    @Test
    @DisplayName("Content-Length가 상한을 넘으면 본문을 읽기 전에 SIZE_EXCEEDED 실패 결과를 돌려준다")
    void declaredLengthOverLimitIsRejected() {
        server.createContext("/", exchange -> respond(exchange, 200, "image/png", new byte[MAX_IMAGE_BYTES + 1], MAX_IMAGE_BYTES + 1));

        assertThat(downloader.download(baseUrl + "/huge.png").failure())
            .isEqualTo(ImageDownloadFailure.SIZE_EXCEEDED);
    }

    private static void respond(HttpExchange exchange, int status, String contentType, byte[] body, long length)
        throws IOException {
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(status, length);
        try (exchange; OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        } catch (IOException ignored) {
        }
    }
}

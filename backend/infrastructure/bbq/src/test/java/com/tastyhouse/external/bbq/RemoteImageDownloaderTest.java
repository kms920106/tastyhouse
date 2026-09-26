package com.tastyhouse.external.bbq;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.tastyhouse.domain.file.model.UploadedFile;
import com.tastyhouse.domain.file.port.FileStoragePort;
import com.tastyhouse.domain.file.repository.UploadedFileRepository;
import com.tastyhouse.domain.file.service.FileUploadService;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RemoteImageDownloaderTest {

    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final byte[] PNG_BYTES = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};

    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<String> requestedRawPath = new AtomicReference<>();
    private final RecordingFileStorage storage = new RecordingFileStorage();
    private RemoteImageDownloader downloader;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        FileUploadService fileUploadService = new FileUploadService(new FixedIdRepository(), storage, event -> { });
        downloader = new RemoteImageDownloader(RestClient.builder(), fileUploadService);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    @DisplayName("퍼센트 인코딩된 URL을 다시 인코딩하지 않고 그대로 요청하며 Content-Type 파라미터를 떼어 업로드한다")
    void keepsPercentEncodedUrlAsIs() {
        server.createContext("/", exchange -> {
            requestedRawPath.set(exchange.getRequestURI().getRawPath());
            respond(exchange, 200, "image/png; charset=binary", PNG_BYTES, PNG_BYTES.length);
        });

        Long fileId = downloader.uploadFromUrl(baseUrl + "/menu/a%20%EC%B9%98%ED%82%A8.png?v=1");

        assertThat(fileId).isEqualTo(1L);
        assertThat(requestedRawPath.get()).isEqualTo("/menu/a%20%EC%B9%98%ED%82%A8.png");
        assertThat(storage.content).containsExactly(PNG_BYTES);
        assertThat(storage.contentType).isEqualTo("image/png");
    }

    @Test
    @DisplayName("200이 아닌 응답은 FILE_EMPTY로 실패한다")
    void nonOkStatusIsFileEmpty() {
        server.createContext("/", exchange -> respond(exchange, 404, "text/plain", new byte[0], -1));

        assertThatThrownBy(() -> downloader.uploadFromUrl(baseUrl + "/missing.png"))
            .isInstanceOfSatisfying(BusinessException.class, e ->
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FILE_EMPTY));
        assertThat(storage.content).isNull();
    }

    @Test
    @DisplayName("Content-Length 없이 상한을 넘겨 흘려보내는 응답도 FILE_SIZE_EXCEEDED로 끊는다")
    void streamedBodyOverLimitIsRejected() {
        server.createContext("/", exchange -> respond(exchange, 200, "image/png", new byte[MAX_IMAGE_BYTES + 1], 0));

        assertThatThrownBy(() -> downloader.uploadFromUrl(baseUrl + "/huge.png"))
            .isInstanceOfSatisfying(BusinessException.class, e ->
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FILE_SIZE_EXCEEDED));
        assertThat(storage.content).isNull();
    }

    @Test
    @DisplayName("Content-Length가 상한을 넘으면 본문을 읽기 전에 FILE_SIZE_EXCEEDED로 실패한다")
    void declaredLengthOverLimitIsRejected() {
        server.createContext("/", exchange -> respond(exchange, 200, "image/png", new byte[MAX_IMAGE_BYTES + 1], MAX_IMAGE_BYTES + 1));

        assertThatThrownBy(() -> downloader.uploadFromUrl(baseUrl + "/huge.png"))
            .isInstanceOfSatisfying(BusinessException.class, e ->
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FILE_SIZE_EXCEEDED));
    }

    private static void respond(HttpExchange exchange, int status, String contentType, byte[] body, long length)
        throws IOException {
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(status, length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        } catch (IOException ignored) {
        } finally {
            exchange.close();
        }
    }

    private static final class RecordingFileStorage implements FileStoragePort {

        private byte[] content;
        private String contentType;

        @Override
        public String store(byte[] content, String storedFilename, String datePath, String contentType) {
            this.content = content;
            this.contentType = contentType;
            return datePath + "/" + storedFilename;
        }

        @Override
        public String getFileUrl(String filePath) {
            return filePath;
        }

        @Override
        public void delete(String filePath) {
        }
    }

    private static final class FixedIdRepository implements UploadedFileRepository {

        @Override
        public UploadedFile save(UploadedFile file) {
            return UploadedFile.reconstitute(
                1L,
                file.getOriginalFilename(),
                file.getStoredFilename(),
                file.getFilePath(),
                file.getFileSize(),
                file.getContentType(),
                null,
                null
            );
        }

        @Override
        public Optional<UploadedFile> findById(UploadedFileId id) {
            return Optional.empty();
        }
    }
}

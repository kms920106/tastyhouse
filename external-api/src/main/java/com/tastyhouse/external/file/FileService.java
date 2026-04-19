package com.tastyhouse.external.file;

import com.tastyhouse.core.entity.file.UploadedFile;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.FileCoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileCoreService fileCoreService;
    private final FileStorageStrategy fileStorageStrategy;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    // 멀티파트 파일을 검증 후 저장소에 업로드하고 DB에 저장된 파일 ID를 반환한다.
    public Long upload(MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + "." + extension;
        String datePath = LocalDate.now().format(DATE_FORMATTER);

        String filePath = fileStorageStrategy.store(file, storedFilename, datePath);

        UploadedFile uploadedFile =
            UploadedFile.of(
                originalFilename,
                storedFilename,
                filePath,
                file.getSize(),
                file.getContentType()
            );

        UploadedFile saved = fileCoreService.save(uploadedFile);
        return saved.getId();
    }

    // 파일 경로로 접근 URL을 반환하며, 경로가 null이면 null을 반환한다.
    public String getUrlByPath(String filePath) {
        if (filePath == null) {
            return null;
        }
        return fileStorageStrategy.getFileUrl(filePath);
    }

    // 파일의 존재 여부, 크기, 콘텐츠 타입, 확장자를 검증한다.
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED);
        }
    }

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

            MultipartFile multipartFile = new ByteArrayMultipartFile(filename, contentType, imageBytes);
            return upload(multipartFile);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("이미지 다운로드 실패: url={}", imageUrl, e);
            throw new RuntimeException("이미지 다운로드 실패: " + imageUrl, e);
        }
    }

    // 파일명에서 소문자로 변환된 확장자를 추출한다.
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException(ErrorCode.FILE_EXTENSION_UNKNOWN);
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}

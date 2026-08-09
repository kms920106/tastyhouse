package com.tastyhouse.ceoapi.file;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.file.FileService;

@Tag(name = "File Ceo", description = "파일 업로드 점주 API")
@RestController
@RequestMapping("/api/files")
public class FileApiController {

    private final FileService fileService;

    public FileApiController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "파일 업로드", description = "파일을 업로드합니다. (jpg, png, gif, webp, pdf / 최대 10MB)")
    @PostMapping(value = "/v1/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> upload(
        @Parameter(description = "업로드할 파일", required = true)
        @RequestParam("file") MultipartFile file
    ) {
        Long fileId = fileService.upload(file);
        return ResponseEntity.ok(ApiResponse.success(fileId));
    }
}

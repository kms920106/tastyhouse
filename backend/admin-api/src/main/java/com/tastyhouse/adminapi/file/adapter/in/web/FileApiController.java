package com.tastyhouse.adminapi.file.adapter.in.web;

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
import com.tastyhouse.adminapplication.file.port.in.FileUploadCommandUseCase;

@Tag(name = "File Admin", description = "파일 업로드 관리자 API")
@RestController
@RequestMapping("/api/files")
public class FileApiController {

    private final FileUploadCommandUseCase fileUploadCommandUseCase;

    public FileApiController(FileUploadCommandUseCase fileUploadCommandUseCase) {
        this.fileUploadCommandUseCase = fileUploadCommandUseCase;
    }

    @Operation(summary = "이미지 파일 업로드", description = "이미지 파일을 업로드합니다. (jpg, png, gif, webp / 최대 10MB)")
    @PostMapping(value = "/v1/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> upload(
        @Parameter(description = "업로드할 이미지 파일", required = true)
        @RequestParam("file") MultipartFile file
    ) {
        Long fileId = fileUploadCommandUseCase.upload(file);
        return ResponseEntity.ok(ApiResponse.success(fileId));
    }
}

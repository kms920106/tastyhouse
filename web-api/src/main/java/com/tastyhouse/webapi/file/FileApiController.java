package com.tastyhouse.webapi.file;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File", description = "파일 업로드 API")
public class FileApiController {

    private final FileService fileService;

    @Operation(summary = "이미지 파일 업로드", description = "이미지 파일을 업로드합니다. (jpg, png, gif, webp / 최대 10MB)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "업로드 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 파일, 허용되지 않는 형식, 크기 초과)")
    })
    @PostMapping(value = "/v1/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<Long>> upload(
        @Parameter(description = "업로드할 이미지 파일", required = true)
        @RequestParam("file") MultipartFile file
    ) {
        Long fileId = fileService.upload(file);
        return ResponseEntity.ok(CommonResponse.success(fileId));
    }
}

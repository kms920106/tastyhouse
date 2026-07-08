package com.tastyhouse.adminapi.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "업로드 파일 정보")
public record FileResponse(
    @Schema(description = "파일 ID", example = "1")
    Long id,

    @Schema(description = "원본 파일명", example = "summer-promotion.png")
    String name,

    @Schema(description = "파일 접근 URL", example = "https://cdn.tastyhouse.com/banner/1.png")
    String url
) {
    public static FileResponse of(Long id, String name, String url) {
        return new FileResponse(id, name, url);
    }
}

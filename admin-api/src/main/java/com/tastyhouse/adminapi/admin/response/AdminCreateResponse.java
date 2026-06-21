package com.tastyhouse.adminapi.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 계정 생성 응답")
public record AdminCreateResponse(
    @Schema(description = "생성된 관리자 ID", example = "1") Long id
) {
}

package com.tastyhouse.adminapi.notice.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "공지사항 노출 여부 변경 요청")
public record NoticeVisibilityRequest(
    @NotNull(message = "노출 여부는 필수입니다.")
    @Schema(description = "노출 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean visible
) {
}

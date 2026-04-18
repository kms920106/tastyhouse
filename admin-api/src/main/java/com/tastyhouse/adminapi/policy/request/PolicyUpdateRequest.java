package com.tastyhouse.adminapi.policy.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "약관 수정 요청")
public record PolicyUpdateRequest(
    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "제목", example = "이용약관 (개정)", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "약관 본문 내용", example = "제1조 (목적) ...", requiredMode = Schema.RequiredMode.REQUIRED)
    String content,

    @NotNull(message = "필수 동의 여부는 필수입니다.")
    @Schema(description = "필수 동의 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean mandatory,

    @NotNull(message = "시행일은 필수입니다.")
    @Schema(description = "시행일", example = "2026-06-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime effectiveDate,

    @Schema(description = "수정자", example = "admin")
    String updatedBy
) {
}

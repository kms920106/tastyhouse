package com.tastyhouse.adminapi.banner.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.core.domain.banner.domain.model.BannerType;

@Schema(description = "배너 등록 요청")
public record BannerCreateRequest(
    @NotNull(message = "배너 유형은 필수입니다.")
    @Schema(description = "배너 유형", example = "HOME", requiredMode = Schema.RequiredMode.REQUIRED)
    BannerType type,

    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
    @Schema(description = "제목", example = "여름 프로모션 배너")
    String title,

    @NotNull(message = "이미지 파일 ID는 필수입니다.")
    @Schema(description = "업로드된 이미지 파일 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long imageFileId,

    @Size(max = 500, message = "링크 URL은 500자를 초과할 수 없습니다.")
    @Schema(description = "클릭 시 이동할 링크 URL", example = "https://example.com/event")
    String linkUrl,

    @Schema(description = "노출 시작 일시", example = "2026-01-01T00:00:00")
    LocalDateTime startDate,

    @Schema(description = "노출 종료 일시", example = "2026-01-31T23:59:59")
    LocalDateTime endDate,

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Schema(description = "정렬 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer sort,

    @Schema(description = "노출 여부 (미지정 시 노출)", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    boolean visible
) {
}

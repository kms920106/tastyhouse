package com.tastyhouse.adminapi.banner.adapter.in.web.request;

import java.time.LocalDateTime;

import com.tastyhouse.application.banner.port.in.BannerUpdateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "배너 수정 요청")
public record BannerUpdateRequest(
    @NotBlank(message = "배너 유형은 필수입니다.")
    @Schema(description = "배너 유형", example = "HOME", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"HOME", "SIDEBAR"})
    String type,

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

    @NotNull(message = "노출 여부는 필수입니다.")
    @Schema(description = "노출 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean visible
) {

    public BannerUpdateCommand toCommand(Long bannerId) {
        return new BannerUpdateCommand(bannerId, type, title, imageFileId, linkUrl, startDate, endDate, sort, visible);
    }
}

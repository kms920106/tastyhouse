package com.tastyhouse.adminapi.banner.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import com.tastyhouse.core.domain.banner.application.dto.BannerAdminListItemDto;

@Schema(description = "배너 목록 항목 응답")
public record BannerListItemResponse(
    @Schema(description = "배너 ID", example = "1")
    Long id,

    @Schema(description = "배너 유형", example = "HOME")
    BannerType type,

    @Schema(description = "제목", example = "여름 프로모션 배너")
    String title,

    @Schema(description = "이미지 URL", example = "https://cdn.example.com/banner/1.png")
    String imageUrl,

    @Schema(description = "클릭 시 이동할 링크 URL", example = "https://example.com/event")
    String linkUrl,

    @Schema(description = "노출 시작 일시", example = "2026-01-01T00:00:00")
    LocalDateTime startDate,

    @Schema(description = "노출 종료 일시", example = "2026-01-31T23:59:59")
    LocalDateTime endDate,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "노출 여부", example = "true")
    boolean visible
) {
    public static BannerListItemResponse from(BannerAdminListItemDto dto) {
        return new BannerListItemResponse(
            dto.id(),
            dto.type(),
            dto.title(),
            dto.filePath(),
            dto.linkUrl(),
            dto.startDate(),
            dto.endDate(),
            dto.sort(),
            dto.visible()
        );
    }
}

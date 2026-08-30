package com.tastyhouse.webapplication.banner.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배너 목록 항목 응답")
public record BannerListItemResponse(
    @Schema(description = "배너 ID", example = "1")
    Long id,

    @Schema(description = "제목", example = "여름 프로모션 배너")
    String title,

    @Schema(description = "이미지 URL", example = "https://cdn.tastyhouse.com/banner/1.png")
    String imageUrl,

    @Schema(description = "클릭 시 이동할 링크 URL", example = "https://tastyhouse.com/event/1")
    String linkUrl
) {
    public static BannerListItemResponse from(
        Long id,
        String title,
        String imageUrl,
        String linkUrl
    ) {
        return new BannerListItemResponse(
            id,
            title,
            imageUrl,
            linkUrl
        );
    }
}

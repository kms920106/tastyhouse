package com.tastyhouse.webapi.event.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이벤트 상세 응답")
public record EventDetailResponse(
    @Schema(description = "배너 이미지 URL", example = "https://example.com/banner.jpg")
    String bannerImageUrl
) {
    public static EventDetailResponse from(String bannerImageUrl) {
        return new EventDetailResponse(bannerImageUrl);
    }
}

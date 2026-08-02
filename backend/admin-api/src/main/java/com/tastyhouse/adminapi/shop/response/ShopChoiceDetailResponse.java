package com.tastyhouse.adminapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "테하 초이스 상세 응답")
public record ShopChoiceDetailResponse(
    @Schema(description = "테하 초이스 ID", example = "1")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "제목", example = "이번 주 추천 맛집")
    String title,

    @Schema(description = "내용", example = "상세 설명 내용...")
    String content
) {
    public static ShopChoiceDetailResponse from(Long id, Long shopId, String title, String content) {
        return new ShopChoiceDetailResponse(id, shopId, title, content);
    }
}

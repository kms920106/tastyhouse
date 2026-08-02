package com.tastyhouse.adminapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "테하 초이스 목록 항목 응답")
public record ShopChoiceListItemResponse(
    @Schema(description = "테하 초이스 ID", example = "1")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "가게명", example = "맛있는 분식")
    String shopName,

    @Schema(description = "제목", example = "이번 주 추천 맛집")
    String title
) {
    public static ShopChoiceListItemResponse from(Long id, Long shopId, String shopName, String title) {
        return new ShopChoiceListItemResponse(id, shopId, shopName, title);
    }
}

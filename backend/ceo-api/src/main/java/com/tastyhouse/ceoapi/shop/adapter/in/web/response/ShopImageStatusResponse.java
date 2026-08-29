package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 이미지(상표/대표이미지) 현황 응답")
public record ShopImageStatusResponse(
    @Schema(description = "현재 적용 중인 이미지 URL(없으면 null)", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2025%2F02%2F16%2Fuuid.jpg?alt=media")
    String currentImageUrl,

    @Schema(description = "변경 요청 목록")
    List<ShopImageChangeRequestItemResponse> requests
) {
    public static ShopImageStatusResponse of(
        String currentImageUrl,
        List<ShopImageChangeRequestItemResponse> requests
    ) {
        return new ShopImageStatusResponse(
            currentImageUrl,
            requests
        );
    }
}

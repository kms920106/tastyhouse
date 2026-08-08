package com.tastyhouse.adminapi.shop.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "라이더 안내 등록 가게 목록 항목")
public record ShopRiderGuideListItemResponse(
    @Schema(description = "가게 ID", example = "5")
    Long shopId,

    @Schema(description = "가게명", example = "맛있는 분식")
    String shopName,

    @Schema(description = "라이더 가게방문 안내 문구 (미등록 시 null)",
        example = "대로변에서 분홍색 건물 1층 OO 안경 옆 가게입니다.")
    String visitGuide,

    @Schema(description = "별도 픽업 위치 설정 여부", example = "true")
    boolean hasPickupLocation,

    @Schema(description = "라이더 안내 정보(문구·픽업 위치) 최종 수정 일시", example = "2026-08-08T21:02:00")
    LocalDateTime updatedAt
) {

    public static ShopRiderGuideListItemResponse from(
        Long shopId,
        String shopName,
        String visitGuide,
        boolean hasPickupLocation,
        LocalDateTime updatedAt
    ) {
        return new ShopRiderGuideListItemResponse(
            shopId,
            shopName,
            visitGuide,
            hasPickupLocation,
            updatedAt
        );
    }
}

package com.tastyhouse.webapi.search.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;

@Schema(description = "매장 검색 목록 아이템 응답")
public record SearchShopListItemResponse(
    @Schema(description = "매장 ID", example = "1")
    Long shopId,

    @Schema(description = "북마크 ID (북마크하지 않은 경우 null)", example = "1")
    Long bookmarkId,

    @Schema(description = "매장명", example = "맛있는 마라탕집")
    String shopName,

    @Schema(description = "인근 역 이름", example = "강남역")
    String stationName,

    @Schema(description = "매장 평점", example = "4.5")
    Double rating,

    @Schema(description = "매장 대표 이미지 URL", example = "https://cdn.tastyhouse.com/shop/1.jpg")
    String imageUrl,

    @Schema(description = "북마크 여부", example = "false")
    boolean bookmarked,

    @Schema(description = "최소주문금액 (0: 미설정, 제한 없음). 배달 주문에만 적용됩니다.", example = "10000")
    int minOrderAmount,

    @Schema(description = "배달팁 최소 금액(원). 구간별·추가 배달팁을 합산한 하한. 0이면 배달팁 없음", example = "2000")
    int minDeliveryTip,

    @Schema(description = "배달팁 최대 금액(원). 고객 주소가 확정되기 전 상한", example = "4000")
    int maxDeliveryTip
) {
    public static SearchShopListItemResponse from(ShopBookmarkedItemResult result) {
        return new SearchShopListItemResponse(
            result.shopId(),
            result.bookmarkId(),
            result.shopName(),
            result.stationName(),
            result.rating(),
            result.imageUrl(),
            result.bookmarked(),
            result.minOrderAmount(),
            result.minDeliveryTip(),
            result.maxDeliveryTip()
        );
    }
}

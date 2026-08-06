package com.tastyhouse.webapi.shop.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최신 매장 목록 아이템 응답")
public record ShopLatestListItemResponse(
    @Schema(description = "매장 ID", example = "1")
    Long id,
    @Schema(description = "매장 이름", example = "타스티하우스 강남점")
    String name,
    @Schema(description = "가까운 역 이름", example = "강남역")
    String stationName,
    @Schema(description = "평균 평점", example = "4.5")
    Double rating,
    @Schema(description = "매장 대표 이미지 URL", example = "https://cdn.tastyhouse.com/shop/1/main.jpg")
    String imageUrl,
    @Schema(description = "매장 등록일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt,
    @Schema(description = "리뷰 개수", example = "10")
    Long reviewCount,
    @Schema(description = "북마크 개수", example = "5")
    Long bookmarkCount,
    @Schema(description = "음식 타입 목록")
    List<String> foodTypes,

    @Schema(description = "실시간 영업 상태(OPEN: 영업중, PREPARING: 준비중)", example = "OPEN")
    String operatingStatus,

    @Schema(description = "최소주문금액 (0: 미설정, 제한 없음). 배달 주문에만 적용됩니다.", example = "10000")
    int minOrderAmount,

    @Schema(description = "배달팁 최소 금액(원). 구간별·추가 배달팁을 합산한 하한. 0이면 배달팁 없음", example = "2000")
    int minDeliveryTip,

    @Schema(description = "배달팁 최대 금액(원). 고객 주소가 확정되기 전 상한", example = "4000")
    int maxDeliveryTip
) {
    public static ShopLatestListItemResponse from(
        Long id,
        String name,
        String stationName,
        Double rating,
        String imageUrl,
        LocalDateTime createdAt,
        Long reviewCount,
        Long bookmarkCount,
        List<String> foodTypes,
        String operatingStatus,
        int minOrderAmount,
        int minDeliveryTip,
        int maxDeliveryTip
    ) {
        return new ShopLatestListItemResponse(
            id,
            name,
            stationName,
            rating,
            imageUrl,
            createdAt,
            reviewCount,
            bookmarkCount,
            foodTypes,
            operatingStatus,
            minOrderAmount,
            minDeliveryTip,
            maxDeliveryTip
        );
    }
}

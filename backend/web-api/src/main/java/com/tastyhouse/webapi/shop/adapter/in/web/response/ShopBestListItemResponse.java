package com.tastyhouse.webapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "베스트 가게 목록 항목 응답")
public record ShopBestListItemResponse(
    @Schema(description = "가게 ID", example = "1")
    Long id,

    @Schema(description = "가게명", example = "BBQ치킨 성내점")
    String name,

    @Schema(description = "인접 지하철역명", example = "강남역")
    String stationName,

    @Schema(description = "평점", example = "4.5")
    Double rating,

    @Schema(description = "가게 썸네일 이미지 URL", example = "https://cdn.tastyhouse.com/shop/1/thumbnail.jpg")
    String imageUrl,

    @Schema(description = "음식 종류 목록")
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
    public static ShopBestListItemResponse from(
        Long id,
        String name,
        String stationName,
        Double rating,
        String imageUrl,
        List<String> foodTypes,
        String operatingStatus,
        int minOrderAmount,
        int minDeliveryTip,
        int maxDeliveryTip
    ) {
        return new ShopBestListItemResponse(
            id,
            name,
            stationName,
            rating,
            imageUrl,
            foodTypes,
            operatingStatus,
            minOrderAmount,
            minDeliveryTip,
            maxDeliveryTip
        );
    }
}

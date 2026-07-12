package com.tastyhouse.webapi.shop.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최신 매장 목록 아이템 응답")
public record LatestShopListItemResponse(
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
    List<String> foodTypes
) {
    public static LatestShopListItemResponse from(
        Long id,
        String name,
        String stationName,
        Double rating,
        String imageUrl,
        LocalDateTime createdAt,
        Long reviewCount,
        Long bookmarkCount,
        List<String> foodTypes
    ) {
        return new LatestShopListItemResponse(
            id,
            name,
            stationName,
            rating,
            imageUrl,
            createdAt,
            reviewCount,
            bookmarkCount,
            foodTypes
        );
    }
}

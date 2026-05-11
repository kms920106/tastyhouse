package com.tastyhouse.webapi.place.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "플레이스 메뉴 응답")
public record PlaceMenuResponse(
    @Schema(description = "메뉴 ID", example = "1")
    Long id,

    @Schema(description = "메뉴명", example = "명란 크림 파스타")
    String name,

    @Schema(description = "이미지 URL", example = "https://example.com/menu.jpg")
    String imageUrl,

    @Schema(description = "원가", example = "18500")
    Integer originalPrice,

    @Schema(description = "할인가", example = "18000")
    Integer discountPrice,

    @Schema(description = "할인율", example = "10")
    BigDecimal discountRate,

    @Schema(description = "메뉴 평점", example = "3.5")
    Double rating,

    @Schema(description = "리뷰 수", example = "24")
    Integer reviewCount,

    @Schema(description = "대표 메뉴 여부", example = "true")
    Boolean isRepresentative,

    @Schema(description = "매운맛 정도 (0-5 또는 0-10)", example = "3")
    Integer spiciness
) {
    public static PlaceMenuResponse from(
        Long id,
        String name,
        String imageUrl,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        Double rating,
        Integer reviewCount,
        Boolean isRepresentative,
        Integer spiciness
    ) {
        return new PlaceMenuResponse(
            id,
            name,
            imageUrl,
            originalPrice,
            discountPrice,
            discountRate,
            rating,
            reviewCount,
            isRepresentative,
            spiciness
        );
    }
}

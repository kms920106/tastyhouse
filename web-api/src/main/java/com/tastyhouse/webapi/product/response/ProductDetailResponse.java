package com.tastyhouse.webapi.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "상품 상세 정보")
public record ProductDetailResponse(
    @Schema(description = "상품 ID", example = "1")
    Long id,

    @Schema(description = "플레이스 ID", example = "100")
    Long placeId,

    @Schema(description = "플레이스명", example = "맛있는 파스타집")
    String placeName,

    @Schema(description = "상품명", example = "명란 크림 파스타")
    String name,

    @Schema(description = "상품 설명", example = "신선한 명란과 크림소스의 조화")
    String description,

    @Schema(description = "이미지 URL 목록", example = "[\"https://example.com/product1.jpg\", \"https://example.com/product2.jpg\"]")
    List<String> imageUrls,

    @Schema(description = "원가", example = "18500")
    Integer originalPrice,

    @Schema(description = "할인가", example = "16650")
    Integer discountPrice,

    @Schema(description = "할인율", example = "10.00")
    BigDecimal discountRate,

    @Schema(description = "상품 평점", example = "4.5")
    Double rating,

    @Schema(description = "리뷰 수", example = "128")
    Integer reviewCount,

    @Schema(description = "대표 메뉴 여부", example = "true")
    Boolean isRepresentative,

    @Schema(description = "품절 여부", example = "false")
    Boolean isSoldOut,

    @Schema(description = "카테고리명", example = "시그니처 메뉴")
    String categoryName
) {
    public static ProductDetailResponse from(
        Long id,
        Long placeId,
        String placeName,
        String name,
        String description,
        List<String> imageUrls,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        Double rating,
        Integer reviewCount,
        Boolean isRepresentative,
        Boolean isSoldOut,
        String categoryName
    ) {
        return new ProductDetailResponse(
            id,
            placeId,
            placeName,
            name,
            description,
            imageUrls,
            originalPrice,
            discountPrice,
            discountRate,
            rating,
            reviewCount,
            isRepresentative,
            isSoldOut,
            categoryName
        );
    }
}

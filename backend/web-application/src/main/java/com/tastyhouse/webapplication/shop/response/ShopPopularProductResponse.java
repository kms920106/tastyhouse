package com.tastyhouse.webapplication.shop.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 가게 상세 상단 "가장 인기 있는 메뉴" 항목.
 *
 * <p>{@code representative}가 {@code true}면 <b>사장님 추천으로 채워진 자리</b>다. 판매량과 무관하게
 * 우선 채워지므로 {@code salesQuantity}가 0일 수 있고, 화면은 이 값으로 추천 뱃지를 붙인다.
 */
@Schema(description = "인기 메뉴 항목")
public record ShopPopularProductResponse(
    @Schema(description = "메뉴 ID", example = "5")
    Long id,

    @Schema(description = "메뉴명", example = "명란 크림 파스타")
    String name,

    @Schema(description = "메뉴 이미지 URL. 없으면 null", example = "https://example.com/menu.jpg")
    String imageUrl,

    @Schema(description = "정가", example = "18500")
    Integer originalPrice,

    @Schema(description = "할인가. 할인이 없으면 null", example = "18000")
    Integer discountPrice,

    @Schema(description = "할인율. 할인이 없으면 null", example = "10")
    BigDecimal discountRate,

    @Schema(description = "메뉴 평점", example = "4.5")
    Double rating,

    @Schema(description = "리뷰 수", example = "24")
    Integer reviewCount,

    @Schema(description = "사장님 추천 여부. true면 추천으로 채워진 자리", example = "true")
    boolean representative,

    @Schema(description = "매운맛 정도", example = "3")
    Integer spiciness,

    @Schema(description = "최근 30일 완료 주문의 판매 수량 합. 추천으로 채워진 항목은 0일 수 있음", example = "42")
    long salesQuantity
) {

    public static ShopPopularProductResponse from(
        Long id,
        String name,
        String imageUrl,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        Double rating,
        Integer reviewCount,
        boolean representative,
        Integer spiciness,
        long salesQuantity
    ) {
        return new ShopPopularProductResponse(
            id,
            name,
            imageUrl,
            originalPrice,
            discountPrice,
            discountRate,
            rating,
            reviewCount,
            representative,
            spiciness,
            salesQuantity
        );
    }
}

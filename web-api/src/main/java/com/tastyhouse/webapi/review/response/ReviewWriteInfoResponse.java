package com.tastyhouse.webapi.review.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 작성 페이지 정보 응답")
public record ReviewWriteInfoResponse(
    @Schema(description = "상품 ID", example = "1")
    Long productId,

    @Schema(description = "상품명", example = "아보카도 햄치즈 샌드위치")
    String productName,

    @Schema(description = "상품 이미지 URL")
    String productImageUrl,

    @Schema(description = "상품 가격", example = "8500")
    Integer productPrice,

    @Schema(description = "주문 ID (주문 기반 리뷰인 경우)", example = "100")
    Long orderId,

    @Schema(description = "이미 리뷰를 작성했는지 여부", example = "false")
    Boolean isReviewed
) {
    public static ReviewWriteInfoResponse from(
        Long productId,
        String productName,
        String productImageUrl,
        Integer productPrice,
        Long orderId,
        Boolean isReviewed
    ) {
        return new ReviewWriteInfoResponse(
            productId,
            productName,
            productImageUrl,
            productPrice,
            orderId,
            isReviewed
        );
    }
}

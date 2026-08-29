package com.tastyhouse.webapi.menureview.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "평가 가능 메뉴 항목")
public record MenuReviewWritableItemResponse(
    @Schema(description = "주문 상품 ID. 등록 요청에 그대로 실어 보내는 키입니다.", example = "10")
    Long orderProductId,

    @Schema(description = "상품 ID", example = "1")
    Long productId,

    @Schema(description = "상품명", example = "황금올리브치킨")
    String productName,

    @Schema(description = "상품 대표 이미지 URL. 이미지가 없으면 null입니다.")
    String productImageUrl,

    @Schema(description = "이미 평가했으면 그 메뉴 평가 ID, 아니면 null", example = "77")
    Long menuReviewId,

    @Schema(description = "이미 평가했으면 그 평점, 아니면 null", example = "5")
    Integer rating,

    @Schema(description = "이미 평가했으면 그 코멘트, 아니면 null", example = "양념이 딱 좋았어요")
    String comment
) {

    public static MenuReviewWritableItemResponse from(
        Long orderProductId,
        Long productId,
        String productName,
        String productImageUrl,
        Long menuReviewId,
        Integer rating,
        String comment
    ) {
        return new MenuReviewWritableItemResponse(
            orderProductId,
            productId,
            productName,
            productImageUrl,
            menuReviewId,
            rating,
            comment
        );
    }
}

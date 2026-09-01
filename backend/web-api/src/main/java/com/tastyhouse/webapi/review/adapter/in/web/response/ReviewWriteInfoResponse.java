package com.tastyhouse.webapi.review.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.webapplication.review.port.out.ReviewWriteInfoView;

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
    boolean reviewed,

    @Schema(
        description = "주문유형. 배달 평가 섹션을 렌더할지 판정하는 데 씁니다 — DELIVERY일 때만 노출합니다. "
            + "주문 정보를 찾을 수 없으면 null입니다.",
        allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"},
        example = "DELIVERY"
    )
    String orderMethod
) {
    public static ReviewWriteInfoResponse from(ReviewWriteInfoView view) {
        return new ReviewWriteInfoResponse(
            view.productId(),
            view.productName(),
            view.productImageUrl(),
            view.productPrice(),
            view.orderId(),
            view.reviewed(),
            view.orderMethod()
        );
    }
}

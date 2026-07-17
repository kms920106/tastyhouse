package com.tastyhouse.adminapi.order.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.order.application.dto.result.OrderProductResult;

@Schema(description = "주문 상품 응답")
public record OrderProductResponse(
    @Schema(description = "주문 상품 ID", example = "1")
    Long orderProductId,

    @Schema(description = "상품 ID", example = "1")
    Long productId,

    @Schema(description = "상품명", example = "후라이드 치킨")
    String name,

    @Schema(description = "상품 이미지 URL", example = "https://cdn.tastyhouse.com/product/1/thumbnail.jpg")
    String imageUrl,

    @Schema(description = "주문 수량", example = "2")
    Integer quantity,

    @Schema(description = "정가", example = "18000")
    Integer originalPrice,

    @Schema(description = "할인가", example = "16000")
    Integer discountPrice,

    @Schema(description = "옵션 금액 합계", example = "2000")
    Integer totalOptionPrice,

    @Schema(description = "총 상품 금액", example = "34000")
    Integer totalPrice,

    @Schema(description = "선택 옵션 목록")
    List<OrderProductOptionResponse> selectedOptions
) {
    public static OrderProductResponse from(OrderProductResult result) {
        List<OrderProductOptionResponse> selectedOptions = result.options() == null ? List.of() :
            result.options().stream()
                .map(OrderProductOptionResponse::from)
                .toList();
        return new OrderProductResponse(
            result.orderProductId().value(),
            result.productId(),
            result.name(),
            result.imageUrl(),
            result.quantity(),
            result.originalPrice(),
            result.discountPrice(),
            result.totalOptionPrice(),
            result.totalPrice(),
            selectedOptions
        );
    }
}

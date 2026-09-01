package com.tastyhouse.webapi.order.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.webapplication.order.port.out.OrderProductViewResult;

@Schema(description = "주문 상품 응답")
public record OrderProductResponse(
    @Schema(description = "주문 상품 ID", example = "1")
    Long orderProductId,

    @Schema(description = "상품 ID", example = "1")
    Long productId,

    @Schema(description = "상품명", example = "후라이드 치킨")
    String name,

    @Schema(description = "주문 시점 가격명 스냅샷. 메뉴 하위 항목으로 표시한다. 가격이 하나뿐인 메뉴는 null",
        example = "곱빼기", nullable = true)
    String priceName,

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

    @Schema(description = "주문 상품 옵션 목록")
    List<OrderProductOptionResponse> options,

    @Schema(description = "리뷰 작성 여부", example = "false")
    boolean reviewed
) {
    public static OrderProductResponse from(OrderProductViewResult result) {
        return new OrderProductResponse(
            result.orderProductId(),
            result.productId(),
            result.name(),
            result.priceName(),
            result.imageUrl(),
            result.quantity(),
            result.originalPrice(),
            result.discountPrice(),
            result.totalOptionPrice(),
            result.totalPrice(),
            result.options().stream()
                .map(OrderProductOptionResponse::from)
                .toList(),
            result.reviewed()
        );
    }
}

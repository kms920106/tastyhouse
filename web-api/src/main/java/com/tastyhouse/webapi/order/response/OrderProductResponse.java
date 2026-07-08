package com.tastyhouse.webapi.order.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.order.application.dto.result.OrderProductOptionResult;
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

    @Schema(description = "주문 상품 옵션 목록")
    List<OrderProductOptionResponse> options,

    @Schema(description = "리뷰 작성 여부", example = "false")
    boolean reviewed
) {
    @Schema(description = "주문 상품 옵션 응답")
    public record OrderProductOptionResponse(
        @Schema(description = "주문 상품 옵션 ID", example = "1")
        Long id,

        @Schema(description = "옵션 그룹명", example = "맵기 선택")
        String optionGroupName,

        @Schema(description = "옵션명", example = "매운맛")
        String optionName,

        @Schema(description = "추가 금액", example = "1000")
        Integer additionalPrice
    ) {
        public static OrderProductOptionResponse from(OrderProductOptionResult option) {
            return new OrderProductOptionResponse(
                option.orderProductOptionId().value(),
                option.optionGroupName(),
                option.optionName(),
                option.additionalPrice()
            );
        }
    }

    public static OrderProductResponse from(OrderProductResult result, String imageUrl, boolean reviewed) {
        List<OrderProductOptionResponse> optionResponses = result.options() == null ? List.of() :
            result.options().stream()
                .map(OrderProductOptionResponse::from)
                .toList();
        return new OrderProductResponse(
            result.orderProductId().value(),
            result.productId(),
            result.name(),
            imageUrl,
            result.quantity(),
            result.originalPrice(),
            result.discountPrice(),
            result.totalOptionPrice(),
            result.totalPrice(),
            optionResponses,
            reviewed
        );
    }
}

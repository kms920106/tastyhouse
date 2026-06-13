package com.tastyhouse.webapi.order.response;

import com.tastyhouse.core.domain.order.application.dto.result.OrderProductOptionResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderProductResult;

import java.util.List;

public record OrderProductResponse(
    Long id,
    Long productId,
    String productName,
    String productImageUrl,
    Integer quantity,
    Integer unitPrice,
    Integer discountPrice,
    Integer optionTotalPrice,
    Integer totalPrice,
    List<OrderProductOptionResponse> options,
    boolean reviewed
) {
    public record OrderProductOptionResponse(
        Long id,
        String optionGroupName,
        String optionName,
        Integer additionalPrice
    ) {
        public static OrderProductOptionResponse from(OrderProductOptionResult option) {
            return new OrderProductOptionResponse(
                option.id(),
                option.optionGroupName(),
                option.optionName(),
                option.additionalPrice()
            );
        }
    }

    public static OrderProductResponse from(OrderProductResult result, String productImageUrl, boolean reviewed) {
        List<OrderProductOptionResponse> optionResponses = result.options() == null ? List.of() :
            result.options().stream()
                .map(OrderProductOptionResponse::from)
                .toList();
        return new OrderProductResponse(
            result.id(),
            result.productId(),
            result.productName(),
            productImageUrl,
            result.quantity(),
            result.unitPrice(),
            result.discountPrice(),
            result.optionTotalPrice(),
            result.totalPrice(),
            optionResponses,
            reviewed
        );
    }
}

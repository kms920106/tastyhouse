package com.tastyhouse.webapi.order.response;

import java.util.List;

import com.tastyhouse.core.domain.order.application.dto.result.OrderProductOptionResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderProductResult;

public record OrderProductResponse(
    Long id,
    Long productId,
    String name,
    String imageUrl,
    Integer quantity,
    Integer originalPrice,
    Integer discountPrice,
    Integer totalOptionPrice,
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

    public static OrderProductResponse from(OrderProductResult result, String imageUrl, boolean reviewed) {
        List<OrderProductOptionResponse> optionResponses = result.options() == null ? List.of() :
            result.options().stream()
                .map(OrderProductOptionResponse::from)
                .toList();
        return new OrderProductResponse(
            result.id(),
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

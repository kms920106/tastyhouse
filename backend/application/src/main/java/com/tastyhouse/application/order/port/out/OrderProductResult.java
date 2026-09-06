package com.tastyhouse.application.order.port.out;

import java.util.List;

public record OrderProductResult(
    Long orderProductId,
    Long productId,
    String name,
    String priceName,
    String imageUrl,
    Integer quantity,
    Integer originalPrice,
    Integer discountPrice,
    Integer totalOptionPrice,
    Integer totalPrice,
    List<OrderProductOptionResult> options
) {
    public OrderProductResult(
        Long orderProductId,
        Long productId,
        String name,
        String priceName,
        String imageUrl,
        Integer quantity,
        Integer originalPrice,
        Integer discountPrice,
        Integer totalOptionPrice,
        Integer totalPrice
    ) {
        this(
            orderProductId,
            productId,
            name,
            priceName,
            imageUrl,
            quantity,
            originalPrice,
            discountPrice,
            totalOptionPrice,
            totalPrice,
            List.of()
        );
    }

    public OrderProductResult withResolvedImageUrl(String resolvedImageUrl, List<OrderProductOptionResult> options) {
        return new OrderProductResult(
            orderProductId,
            productId,
            name,
            priceName,
            resolvedImageUrl,
            quantity,
            originalPrice,
            discountPrice,
            totalOptionPrice,
            totalPrice,
            options
        );
    }
}

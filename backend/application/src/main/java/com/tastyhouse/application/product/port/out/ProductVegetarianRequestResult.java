package com.tastyhouse.application.product.port.out;

public record ProductVegetarianRequestResult(
    Long id,
    Long productId,
    Long shopId,
    String productName,
    String vegetarianType,
    String ingredients,
    String description,
    String status,
    String rejectReason
) {
}

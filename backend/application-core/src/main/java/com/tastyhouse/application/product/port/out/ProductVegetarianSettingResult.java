package com.tastyhouse.application.product.port.out;

public record ProductVegetarianSettingResult(
    Long productId,
    Long shopId,
    String vegetarianType
) {
}

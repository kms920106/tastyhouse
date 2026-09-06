package com.tastyhouse.application.product.port.out;

import com.tastyhouse.domain.product.model.VegetarianType;

public record ProductVegetarianSettingResult(
    Long productId,
    Long shopId,
    VegetarianType vegetarianType
) {
}

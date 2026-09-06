package com.tastyhouse.application.product.port.out;

import java.util.List;

public record ProductAvailabilityGroupResult(
    Long categoryId,
    String categoryName,
    Integer categorySort,
    List<ProductAvailabilityItemResult> products
) {
}

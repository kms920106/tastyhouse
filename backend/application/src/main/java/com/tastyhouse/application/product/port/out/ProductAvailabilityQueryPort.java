package com.tastyhouse.application.product.port.out;

import java.util.List;

public interface ProductAvailabilityQueryPort {

    List<ProductAvailabilityItemResult> findProductAvailability(ProductAvailabilitySearchCondition condition);

    List<ProductOptionAvailabilityGroupResult> findProductOptionAvailability(
        ProductAvailabilitySearchCondition condition,
        String normalOptionType,
        String commonOptionType
    );
}

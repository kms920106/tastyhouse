package com.tastyhouse.application.product.port.out;

import java.util.List;

public record ProductOptionGroupLinkedProductsResult(
    Long optionGroupId,
    List<ProductOptionGroupLinkedProductResult> products
) {
}

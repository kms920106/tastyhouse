package com.tastyhouse.application.product.port.out;

import java.util.List;

public record ProductBatchItemView(
    Long id,
    boolean available,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
    List<BatchOptionResult> options,
    List<ProductPriceView> prices
) {
}

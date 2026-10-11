package com.tastyhouse.application.product.port.out;

public record ProductPriceView(
    Long priceId,
    String priceName,
    Integer price
) {
}

package com.tastyhouse.application.product.port.out.write;

public record ProductOptionGroupLinkState(
    Long id,
    Long productId,
    Long optionGroupId,
    Integer sort
) {
}

package com.tastyhouse.application.product.port.out.write;

public record ProductCommonOptionGroupLinkState(
    Long id,
    Long productId,
    Long optionGroupId,
    Integer sort
) {
}

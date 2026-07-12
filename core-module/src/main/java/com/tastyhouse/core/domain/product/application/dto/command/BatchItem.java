package com.tastyhouse.core.domain.product.application.dto.command;

public record BatchItem(
    Long productId,
    Long optionId
) {

    public static BatchItem of(Long productId, Long optionId) {
        return new BatchItem(productId, optionId);
    }
}

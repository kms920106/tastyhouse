package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.domain.product.model.StorePriceUnverifiedReason;

public record ShopStorePriceVerificationViewResult(
    Long id,
    String status,
    boolean verified,
    String rejectReason,
    List<UnverifiedItem> unverifiedItems
) {

    public record UnverifiedItem(
        Long productId,
        String productName,
        StorePriceUnverifiedReason reason
    ) {
    }
}

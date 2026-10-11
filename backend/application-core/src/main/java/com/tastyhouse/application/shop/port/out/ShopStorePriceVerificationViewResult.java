package com.tastyhouse.application.shop.port.out;

import java.util.List;

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
        String reason
    ) {
    }
}

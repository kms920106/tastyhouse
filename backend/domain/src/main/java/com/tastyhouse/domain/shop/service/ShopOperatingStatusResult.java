package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.shop.model.OrderUnavailableReason;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;

public record ShopOperatingStatusResult(
    ShopOperatingStatus status,
    OrderUnavailableReason unavailableReason
) {
    public static ShopOperatingStatusResult open() {
        return new ShopOperatingStatusResult(ShopOperatingStatus.OPEN, null);
    }

    public static ShopOperatingStatusResult preparing(OrderUnavailableReason reason) {
        return new ShopOperatingStatusResult(
            ShopOperatingStatus.PREPARING,
            reason
        );
    }

    public boolean isOpen() {
        return status == ShopOperatingStatus.OPEN;
    }
}

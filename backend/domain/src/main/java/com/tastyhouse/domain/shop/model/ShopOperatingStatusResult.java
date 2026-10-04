package com.tastyhouse.domain.shop.model;

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

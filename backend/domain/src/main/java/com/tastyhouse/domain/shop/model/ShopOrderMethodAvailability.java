package com.tastyhouse.domain.shop.model;

public record ShopOrderMethodAvailability(
    ShopOperatingStatusResult shopWide,
    ShopOperatingStatusResult orderMethod
) {
}

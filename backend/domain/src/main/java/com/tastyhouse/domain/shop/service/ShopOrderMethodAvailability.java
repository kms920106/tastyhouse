package com.tastyhouse.domain.shop.service;

public record ShopOrderMethodAvailability(
    ShopOperatingStatusResult shopWide,
    ShopOperatingStatusResult orderMethod
) {
}

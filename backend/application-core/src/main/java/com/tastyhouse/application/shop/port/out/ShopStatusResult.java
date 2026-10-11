package com.tastyhouse.application.shop.port.out;

public record ShopStatusResult(
    boolean hidden,
    boolean permanentlyClosed
) {
}

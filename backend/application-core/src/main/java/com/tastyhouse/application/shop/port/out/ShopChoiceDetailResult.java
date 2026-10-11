package com.tastyhouse.application.shop.port.out;

public record ShopChoiceDetailResult(
    Long id,
    Long shopId,
    String title,
    String content
) {
}

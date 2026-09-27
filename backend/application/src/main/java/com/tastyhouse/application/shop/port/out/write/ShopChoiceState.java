package com.tastyhouse.application.shop.port.out.write;

public record ShopChoiceState(
    Long id,
    Long shopId,
    String title,
    String content
) {
}

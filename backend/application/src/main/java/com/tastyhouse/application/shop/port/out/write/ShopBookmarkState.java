package com.tastyhouse.application.shop.port.out.write;

public record ShopBookmarkState(
    Long id,
    Long shopId,
    Long memberId
) {
}

package com.tastyhouse.application.shop.port.out;

public record ShopOrderNoticeResult(
    Long id,
    Long shopId,
    String content,
    boolean hidden,
    String hiddenReason
) {
}

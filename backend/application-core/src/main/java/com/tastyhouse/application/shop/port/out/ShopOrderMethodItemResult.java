package com.tastyhouse.application.shop.port.out;

public record ShopOrderMethodItemResult(
    String code,
    String name,
    boolean orderable,
    String unavailableReason,
    String unavailableReasonName
) {
}

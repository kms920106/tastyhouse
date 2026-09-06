package com.tastyhouse.domain.order.service;

public record OrderLineOptionAmounts(
    int totalOptionPrice,
    int totalDepositAmount,
    int totalPersonalCupDiscount
) {
}

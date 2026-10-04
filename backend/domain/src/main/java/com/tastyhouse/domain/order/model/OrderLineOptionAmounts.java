package com.tastyhouse.domain.order.model;

public record OrderLineOptionAmounts(
    int totalOptionPrice,
    int totalDepositAmount,
    int totalPersonalCupDiscount
) {
}

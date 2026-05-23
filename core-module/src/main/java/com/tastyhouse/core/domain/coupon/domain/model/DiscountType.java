package com.tastyhouse.core.domain.coupon.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiscountType {
    AMOUNT("정액 할인"),
    RATE("정률 할인");

    private final String description;
}

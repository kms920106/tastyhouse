package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;

public record ProductOptionState(
    Long id,
    Long optionGroupId,
    String name,
    Integer additionalPrice,
    Integer sort,
    boolean soldOut,
    LocalDateTime soldOutUntil,
    boolean visible,
    Integer cupCount,
    Integer personalCupDiscountAmount
) {
}

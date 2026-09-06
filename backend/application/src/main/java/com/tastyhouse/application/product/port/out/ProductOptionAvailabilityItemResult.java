package com.tastyhouse.application.product.port.out;

import java.time.LocalDateTime;

public record ProductOptionAvailabilityItemResult(
    Long id,
    String optionType,
    String name,
    Integer additionalPrice,
    boolean soldOut,
    LocalDateTime soldOutUntil,
    boolean visible,
    Integer sort
) {
}

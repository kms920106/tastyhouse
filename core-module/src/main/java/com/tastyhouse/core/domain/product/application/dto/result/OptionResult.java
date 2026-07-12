package com.tastyhouse.core.domain.product.application.dto.result;

public record OptionResult(
    Long id,
    String name,
    Integer additionalPrice,
    boolean soldOut
) {
}

package com.tastyhouse.core.domain.product.application.dto.command;

public record SaveProductOptionCommand(
    Long optionGroupId,
    String name,
    Integer additionalPrice,
    Integer sort,
    boolean soldOut,
    boolean visible
) {}

package com.tastyhouse.core.domain.product.application.dto.command;

public record SaveProductOptionCommand(
    Long optionGroupId,
    String name,
    Integer additionalPrice,
    Integer sort,
    boolean soldOut,
    boolean visible
) {

    public static SaveProductOptionCommand of(
        Long optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        boolean visible
    ) {
        return new SaveProductOptionCommand(optionGroupId, name, additionalPrice, sort, soldOut, visible);
    }
}

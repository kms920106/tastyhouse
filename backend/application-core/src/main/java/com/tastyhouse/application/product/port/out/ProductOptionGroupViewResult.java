package com.tastyhouse.application.product.port.out;

import java.util.List;

public record ProductOptionGroupViewResult(
    Long id,
    String name,
    String description,
    Boolean required,
    Boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect,
    Integer sort,
    Boolean visible,
    String groupType,
    Long linkedProductCount,
    List<Option> options
) {

    public record Option(
        Long id,
        String name,
        Integer additionalPrice,
        Integer sort,
        Boolean visible,
        Integer cupCount,
        Integer depositAmount,
        Integer personalCupDiscountAmount
    ) {
    }
}

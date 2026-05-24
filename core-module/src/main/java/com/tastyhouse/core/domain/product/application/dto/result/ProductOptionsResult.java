package com.tastyhouse.core.domain.product.application.dto.result;

import java.util.List;

public record ProductOptionsResult(List<OptionGroupResult> optionGroups) {

    public record OptionGroupResult(
        Long id,
        String name,
        String description,
        Boolean isRequired,
        Boolean isMultipleSelect,
        Integer minSelect,
        Integer maxSelect,
        boolean isCommon,
        List<OptionResult> options
    ) {}

    public record OptionResult(
        Long id,
        String name,
        Integer additionalPrice,
        Boolean isSoldOut
    ) {}
}

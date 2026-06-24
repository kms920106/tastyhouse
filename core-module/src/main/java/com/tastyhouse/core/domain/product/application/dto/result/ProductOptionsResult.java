package com.tastyhouse.core.domain.product.application.dto.result;

import java.util.List;

public record ProductOptionsResult(List<OptionGroupResult> optionGroups) {

    public record OptionGroupResult(
        Long id,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        boolean common,
        List<OptionResult> options
    ) {}

    public record OptionResult(
        Long id,
        String name,
        Integer additionalPrice,
        boolean soldOut
    ) {}
}

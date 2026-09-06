package com.tastyhouse.application.product.port.out;

import java.util.List;

public record ProductOptionGroupMergeSuggestionResult(
    String signature,
    String name,
    Integer minSelect,
    Integer maxSelect,
    Integer groupCount,
    Integer linkedProductCount,
    List<Option> options,
    List<Group> groups
) {

    public record Option(
        Long id,
        String name,
        Integer additionalPrice
    ) {
    }

    public record Group(
        Long id,
        Integer linkedProductCount,
        List<String> linkedProductNames
    ) {
    }
}

package com.tastyhouse.application.product.port.out;

import java.util.List;

public record ProductOptionGroupMergePreviewResult(
    Group base,
    List<Group> candidates,
    boolean mergeable,
    String blockedReason
) {

    public record Group(
        Long id,
        String name,
        String description,
        Boolean required,
        Boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        List<String> linkedProductNames,
        Boolean nameDiffers,
        Boolean minSelectDiffers,
        Boolean maxSelectDiffers,
        List<Option> options
    ) {
    }

    public record Option(
        Long id,
        String name,
        Integer additionalPrice,
        Boolean soldOut,
        Boolean visible,
        String diffType
    ) {
    }
}

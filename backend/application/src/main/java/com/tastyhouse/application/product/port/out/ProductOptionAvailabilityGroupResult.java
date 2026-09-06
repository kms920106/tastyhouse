package com.tastyhouse.application.product.port.out;

import java.util.List;

public record ProductOptionAvailabilityGroupResult(
    Long optionGroupId,
    String optionType,
    String name,
    boolean required,
    Integer minSelect,
    Integer maxSelect,
    List<String> linkedProductNames,
    Integer sort,
    List<ProductOptionAvailabilityItemResult> options
) {
}

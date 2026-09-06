package com.tastyhouse.application.product.port.out;

import java.util.List;

public record ProductOptionGroupManagementResult(
    Long id,
    String name,
    String description,
    boolean required,
    boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect,
    Integer sort,
    boolean visible,
    String groupType,
    long linkedProductCount,
    List<ProductOptionManagementResult> options
) {
}

package com.tastyhouse.application.product.port.out;

import java.util.List;

public record OptionGroupResult(
    Long id,
    String name,
    String description,
    boolean required,
    boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect,
    boolean common,
    String groupType,
    List<OptionResult> options
) {
}

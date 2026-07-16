package com.tastyhouse.core.domain.faq.application.dto.command;

public record FaqCategoryCreateCommand(
    String name,
    Integer sort,
    boolean visible
) {

    public static FaqCategoryCreateCommand of(String name, Integer sort, boolean visible) {
        return new FaqCategoryCreateCommand(name, sort, visible);
    }
}

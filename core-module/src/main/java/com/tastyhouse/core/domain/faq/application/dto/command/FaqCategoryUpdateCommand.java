package com.tastyhouse.core.domain.faq.application.dto.command;

public record FaqCategoryUpdateCommand(
    String name,
    Integer sort,
    boolean visible
) {

    public static FaqCategoryUpdateCommand of(String name, Integer sort, boolean visible) {
        return new FaqCategoryUpdateCommand(name, sort, visible);
    }
}

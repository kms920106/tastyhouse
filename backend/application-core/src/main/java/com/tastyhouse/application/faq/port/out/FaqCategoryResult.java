package com.tastyhouse.application.faq.port.out;

public record FaqCategoryResult(
    Long id,
    String name,
    Integer sort
) {
}

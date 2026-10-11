package com.tastyhouse.application.faq.port.out;

public record FaqResult(
    Long id,
    Long faqCategoryId,
    String question,
    String answer,
    Integer sort
) {
}

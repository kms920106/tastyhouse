package com.tastyhouse.application.faq.port.out;

public record FaqSearchCondition(
    Long categoryId,
    String question,
    Boolean visible
) {

    public static FaqSearchCondition of(Long categoryId, String question, Boolean visible) {
        return new FaqSearchCondition(categoryId, question, visible);
    }
}

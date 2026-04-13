package com.tastyhouse.webapi.faq.response;

public record FaqItem(
    Long id,
    Long categoryId,
    String question,
    String answer,
    Integer sort
) {
    public static FaqItem from(
        Long id,
        Long categoryId,
        String question,
        String answer,
        Integer sort
    ) {
        return new FaqItem(
            id,
            categoryId,
            question,
            answer,
            sort
        );
    }
}

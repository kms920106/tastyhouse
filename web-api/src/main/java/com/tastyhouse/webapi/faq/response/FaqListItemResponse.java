package com.tastyhouse.webapi.faq.response;

public record FaqListItemResponse(
    Long id,
    Long categoryId,
    String question,
    String answer,
    Integer sort
) {
    public static FaqListItemResponse from(
        Long id,
        Long categoryId,
        String question,
        String answer,
        Integer sort
    ) {
        return new FaqListItemResponse(
            id,
            categoryId,
            question,
            answer,
            sort
        );
    }
}

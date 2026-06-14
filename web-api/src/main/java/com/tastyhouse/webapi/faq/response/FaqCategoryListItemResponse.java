package com.tastyhouse.webapi.faq.response;

public record FaqCategoryListItemResponse(
    Long id,
    String name,
    Integer sort
) {
    public static FaqCategoryListItemResponse from(
        Long id,
        String name,
        Integer sort
    ) {
        return new FaqCategoryListItemResponse(
            id,
            name,
            sort
        );
    }
}

package com.tastyhouse.webapi.faq.response;

public record FaqCategoryItem(
    Long id,
    String name,
    Integer sort
) {
    public static FaqCategoryItem from(
    Long id,
    String name,
    Integer sort
    ) {
    return new FaqCategoryItem(
        id,
        name,
        sort
    );
    }
}

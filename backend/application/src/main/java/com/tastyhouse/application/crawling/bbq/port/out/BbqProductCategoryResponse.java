package com.tastyhouse.application.crawling.bbq.port.out;

public record BbqProductCategoryResponse(
    Long id,
    Long shopId,
    String name,
    Integer sort,
    boolean visible
) {
    public static BbqProductCategoryResponse from(
        Long id,
        Long shopId,
        String name,
        Integer sort,
        boolean visible
    ) {
        return new BbqProductCategoryResponse(
            id,
            shopId,
            name,
            sort,
            visible
        );
    }
}

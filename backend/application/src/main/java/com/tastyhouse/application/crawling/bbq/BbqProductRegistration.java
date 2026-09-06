package com.tastyhouse.application.crawling.bbq;

public record BbqProductRegistration(
    Long shopId,
    Long productCategoryId,
    String name,
    String description,
    Integer originalPrice,
    boolean soldOut,
    Integer sort,
    Long imageFileId,
    Long bbqMenuId,
    Long bbqCategoryId
) {

    public static BbqProductRegistration of(
        Long shopId,
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        boolean soldOut,
        Integer sort,
        Long imageFileId,
        Long bbqMenuId,
        Long bbqCategoryId
    ) {
        return new BbqProductRegistration(
            shopId,
            productCategoryId,
            name,
            description,
            originalPrice,
            soldOut,
            sort,
            imageFileId,
            bbqMenuId,
            bbqCategoryId
        );
    }
}

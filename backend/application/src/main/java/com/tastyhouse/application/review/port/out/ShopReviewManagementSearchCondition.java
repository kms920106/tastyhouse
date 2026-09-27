package com.tastyhouse.application.review.port.out;

import java.time.LocalDate;

public record ShopReviewManagementSearchCondition(
    Long shopId,
    ShopReviewTabFilter tab,
    LocalDate startDate,
    LocalDate endDate,
    Integer rating,
    String orderMethod,
    Boolean hasImage,
    ReviewSortSpec sort
) {

    public static ShopReviewManagementSearchCondition of(
        Long shopId,
        ShopReviewTabFilter tab,
        LocalDate startDate,
        LocalDate endDate,
        Integer rating,
        String orderMethod,
        Boolean hasImage,
        ReviewSortSpec sort
    ) {
        return new ShopReviewManagementSearchCondition(
            shopId,
            tab,
            startDate,
            endDate,
            rating,
            orderMethod,
            hasImage,
            sort
        );
    }
}

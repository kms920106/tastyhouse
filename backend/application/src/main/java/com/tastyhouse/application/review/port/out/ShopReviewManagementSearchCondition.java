package com.tastyhouse.application.review.port.out;

import java.time.LocalDate;

public record ShopReviewManagementSearchCondition(
    Long shopId,
    String tab,
    LocalDate startDate,
    LocalDate endDate,
    Integer rating,
    String orderMethod,
    Boolean hasImage,
    String sortType
) {

    public static ShopReviewManagementSearchCondition of(
        Long shopId,
        String tab,
        LocalDate startDate,
        LocalDate endDate,
        Integer rating,
        String orderMethod,
        Boolean hasImage,
        String sortType
    ) {
        return new ShopReviewManagementSearchCondition(
            shopId,
            tab,
            startDate,
            endDate,
            rating,
            orderMethod,
            hasImage,
            sortType
        );
    }
}

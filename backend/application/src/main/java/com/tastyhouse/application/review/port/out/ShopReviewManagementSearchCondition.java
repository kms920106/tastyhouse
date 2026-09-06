package com.tastyhouse.application.review.port.out;

import java.time.LocalDate;

import com.tastyhouse.domain.review.model.ReviewListTab;
import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.shared.model.OrderMethod;

public record ShopReviewManagementSearchCondition(
    Long shopId,
    ReviewListTab tab,
    LocalDate startDate,
    LocalDate endDate,
    Integer rating,
    OrderMethod orderMethod,
    Boolean hasImage,
    ReviewSortType sortType
) {

    public static ShopReviewManagementSearchCondition of(
        Long shopId,
        ReviewListTab tab,
        LocalDate startDate,
        LocalDate endDate,
        Integer rating,
        OrderMethod orderMethod,
        Boolean hasImage,
        ReviewSortType sortType
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

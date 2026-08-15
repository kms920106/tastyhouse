package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDate;

import com.tastyhouse.domain.review.model.ReviewListTab;
import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.shared.model.OrderMethod;

/**
 * 점주 리뷰 목록 조회 조건.
 *
 * <p>enum은 소비 모듈 Service가 {@code Enum.from(String)}으로 승격한 뒤 넘긴다(도메인 enum 경계 규칙).
 * 식별자는 query 계층 규칙대로 raw {@code Long}이다.
 */
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

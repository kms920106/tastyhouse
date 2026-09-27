package com.tastyhouse.application.review.service;

import com.tastyhouse.domain.review.model.ReviewListTab;
import com.tastyhouse.application.review.port.out.ShopReviewTabFilter;

public final class ShopReviewTabFilters {

    private ShopReviewTabFilters() {
    }

    public static ShopReviewTabFilter of(ReviewListTab tab) {
        return switch (tab) {
            case ALL -> new ShopReviewTabFilter(false, false, false);
            case UNANSWERED -> new ShopReviewTabFilter(true, false, false);
            case BLINDED -> new ShopReviewTabFilter(false, true, false);
            case OWNER_ONLY -> new ShopReviewTabFilter(false, false, true);
        };
    }
}

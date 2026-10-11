package com.tastyhouse.application.review.service;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.application.review.port.out.ReviewSortSpec;

public final class ReviewSortSpecs {

    private ReviewSortSpecs() {
    }

    public static ReviewSortSpec of(ReviewSortType sortType) {
        return switch (sortType) {
            case RECOMMENDED -> new ReviewSortSpec(true, false);
            case LATEST -> new ReviewSortSpec(false, false);
            case OLDEST -> new ReviewSortSpec(false, true);
        };
    }
}

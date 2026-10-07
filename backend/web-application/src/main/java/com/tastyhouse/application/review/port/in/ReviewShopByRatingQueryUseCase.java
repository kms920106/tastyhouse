package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;

public interface ReviewShopByRatingQueryUseCase {

    ReviewsByRatingResult findShopReviewsByRating(Long shopId, int page, int size, Boolean hasImage, String sortType);
}

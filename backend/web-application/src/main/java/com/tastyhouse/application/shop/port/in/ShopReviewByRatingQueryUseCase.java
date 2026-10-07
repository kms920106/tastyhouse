package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;

public interface ShopReviewByRatingQueryUseCase {

    ReviewsByRatingResult getShopReviewsByRatingWithPagination(Long shopId, int page, int size, Boolean hasImage, String sortType);
}

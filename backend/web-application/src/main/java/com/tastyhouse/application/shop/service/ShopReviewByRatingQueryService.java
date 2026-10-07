package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewShopByRatingQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;
import com.tastyhouse.application.shop.port.in.ShopReviewByRatingQueryUseCase;

@Service
@Transactional(readOnly = true)
class ShopReviewByRatingQueryService implements ShopReviewByRatingQueryUseCase {

    private final ReviewShopByRatingQueryUseCase reviewShopByRatingQueryUseCase;

    public ShopReviewByRatingQueryService(ReviewShopByRatingQueryUseCase reviewShopByRatingQueryUseCase) {
        this.reviewShopByRatingQueryUseCase = reviewShopByRatingQueryUseCase;
    }

    @Override
    public ReviewsByRatingResult getShopReviewsByRatingWithPagination(
        Long shopId,
        int page,
        int size,
        Boolean hasImage,
        String sortType
    ) {
        return reviewShopByRatingQueryUseCase.findShopReviewsByRating(shopId, page, size, hasImage, sortType);
    }
}

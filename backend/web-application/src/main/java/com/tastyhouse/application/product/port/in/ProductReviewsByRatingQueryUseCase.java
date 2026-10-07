package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;

public interface ProductReviewsByRatingQueryUseCase {

    ReviewsByRatingResult getProductReviewsByRatingWithPagination(Long productId, int page, int size, Boolean hasImage);
}

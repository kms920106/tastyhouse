package com.tastyhouse.core.domain.review.domain.repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewProduct;

public interface ReviewProductRepository {

    ReviewProduct save(ReviewProduct reviewProduct);
}

package com.tastyhouse.core.domain.review.domain.repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewProduct;

import java.util.List;

public interface ReviewProductRepository {

    ReviewProduct save(ReviewProduct reviewProduct);

    List<ReviewProduct> findByReviewId(Long reviewId);

    void deleteByReviewId(Long reviewId);
}

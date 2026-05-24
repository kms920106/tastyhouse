package com.tastyhouse.core.domain.review.domain.repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewImage;

import java.util.List;

public interface ReviewImageRepository {

    List<ReviewImage> findByReviewIdOrderBySortAsc(Long reviewId);

    List<ReviewImage> findByReviewIdInOrderBySortAsc(List<Long> reviewIds);

    void saveAll(List<ReviewImage> images);

    void deleteByReviewId(Long reviewId);
}

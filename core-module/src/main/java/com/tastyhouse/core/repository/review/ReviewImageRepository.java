package com.tastyhouse.core.repository.review;

import com.tastyhouse.core.entity.review.ReviewImage;

import java.util.List;

public interface ReviewImageRepository {

    List<ReviewImage> findByReviewIdOrderBySortAsc(Long reviewId);

    List<ReviewImage> findByReviewIdInOrderBySortAsc(List<Long> reviewIds);

    void saveAll(List<ReviewImage> images);

    void deleteByReviewId(Long reviewId);
}

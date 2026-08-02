package com.tastyhouse.domain.review.repository;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewImage;
import com.tastyhouse.domain.review.vo.ReviewId;

public interface ReviewImageRepository {

    void saveAll(List<ReviewImage> images);

    void deleteByReviewId(ReviewId reviewId);
}

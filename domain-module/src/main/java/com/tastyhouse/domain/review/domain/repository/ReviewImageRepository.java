package com.tastyhouse.domain.review.domain.repository;

import java.util.List;

import com.tastyhouse.domain.review.domain.model.ReviewImage;
import com.tastyhouse.domain.review.domain.vo.ReviewId;

public interface ReviewImageRepository {

    void saveAll(List<ReviewImage> images);

    void deleteByReviewId(ReviewId reviewId);
}

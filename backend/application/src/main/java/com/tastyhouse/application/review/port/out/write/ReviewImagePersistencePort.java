package com.tastyhouse.application.review.port.out.write;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewImage;
import com.tastyhouse.domain.review.vo.ReviewId;

public interface ReviewImagePersistencePort {

    void saveAll(List<ReviewImage> images);

    void deleteByReviewId(ReviewId reviewId);
}

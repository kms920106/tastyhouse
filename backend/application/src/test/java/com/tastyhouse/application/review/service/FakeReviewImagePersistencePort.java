package com.tastyhouse.application.review.service;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewImage;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.out.write.ReviewImagePersistencePort;

public class FakeReviewImagePersistencePort implements ReviewImagePersistencePort {
    @Override
    public void saveAll(List<ReviewImage> images) {
    }

    @Override
    public void deleteByReviewId(ReviewId reviewId) {
    }
}

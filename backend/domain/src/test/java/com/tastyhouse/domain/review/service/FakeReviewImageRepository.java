package com.tastyhouse.domain.review.service;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewImage;
import com.tastyhouse.domain.review.repository.ReviewImageRepository;
import com.tastyhouse.domain.review.vo.ReviewId;

public class FakeReviewImageRepository implements ReviewImageRepository {
    @Override
    public void saveAll(List<ReviewImage> images) {
    }

    @Override
    public void deleteByReviewId(ReviewId reviewId) {
    }
}

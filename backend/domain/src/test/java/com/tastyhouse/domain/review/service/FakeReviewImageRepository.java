package com.tastyhouse.domain.review.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.review.model.ReviewImage;
import com.tastyhouse.domain.review.repository.ReviewImageRepository;
import com.tastyhouse.domain.review.vo.ReviewId;

/**
 * 리뷰 이미지 write 포트의 인메모리 fake.
 */
public class FakeReviewImageRepository implements ReviewImageRepository {

    private final List<ReviewImage> images = new ArrayList<>();

    @Override
    public void saveAll(List<ReviewImage> images) {
        this.images.addAll(images);
    }

    @Override
    public void deleteByReviewId(ReviewId reviewId) {
        images.removeIf(image -> image.getReviewId().equals(reviewId));
    }
}

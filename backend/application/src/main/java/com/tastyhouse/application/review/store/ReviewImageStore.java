package com.tastyhouse.application.review.store;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewImage;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.out.write.ReviewImageStatePort;

public class ReviewImageStore implements ReviewImageRepository {
    private final ReviewImageStatePort reviewImageStatePort;

    public ReviewImageStore(ReviewImageStatePort reviewImageStatePort) {
        this.reviewImageStatePort = reviewImageStatePort;
    }

    @Override
    public void saveAll(List<ReviewImage> images) {
        reviewImageStatePort.saveAll(images.stream()
            .map(ReviewImageStateMapper::toState)
            .toList());
    }

    @Override
    public void deleteByReviewId(ReviewId reviewId) {
        reviewImageStatePort.deleteByReviewId(reviewId.value());
    }
}

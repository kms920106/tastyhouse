package com.tastyhouse.application.review.store;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.out.write.ReviewTagStatePort;

public class ReviewTagStore implements ReviewTagRepository {
    private final ReviewTagStatePort reviewTagStatePort;

    public ReviewTagStore(ReviewTagStatePort reviewTagStatePort) {
        this.reviewTagStatePort = reviewTagStatePort;
    }

    @Override
    public void saveAll(List<ReviewTag> tags) {
        reviewTagStatePort.saveAll(tags.stream()
            .map(ReviewTagStateMapper::toState)
            .toList());
    }

    @Override
    public void deleteByReviewId(ReviewId reviewId) {
        reviewTagStatePort.deleteByReviewId(reviewId.value());
    }
}

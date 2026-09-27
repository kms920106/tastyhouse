package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.application.review.port.out.write.ReviewImageState;

final class ReviewImageMapper {
    private ReviewImageMapper() {
    }

    static ReviewImageJpaEntity toEntity(ReviewImageState state) {
        return ReviewImageJpaEntity.create(
            state.reviewId(),
            state.imageFileId(),
            state.sort()
        );
    }
}

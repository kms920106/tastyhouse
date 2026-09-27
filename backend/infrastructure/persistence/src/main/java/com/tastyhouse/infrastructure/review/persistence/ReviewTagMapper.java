package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.application.review.port.out.write.ReviewTagState;

final class ReviewTagMapper {
    private ReviewTagMapper() {
    }

    static ReviewTagJpaEntity toEntity(ReviewTagState state) {
        return ReviewTagJpaEntity.create(
            state.reviewId(),
            state.tagId()
        );
    }
}

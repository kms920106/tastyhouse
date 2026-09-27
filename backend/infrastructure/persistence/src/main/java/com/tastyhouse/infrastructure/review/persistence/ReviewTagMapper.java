package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.review.model.ReviewTag;

final class ReviewTagMapper {
    private ReviewTagMapper() {
    }

    static ReviewTagJpaEntity toEntity(ReviewTag reviewTag) {
        return ReviewTagJpaEntity.create(
            reviewTag.getReviewId() == null ? null : reviewTag.getReviewId().value(),
            reviewTag.getTagId() == null ? null : reviewTag.getTagId().value()
        );
    }
}

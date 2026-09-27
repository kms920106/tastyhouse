package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.review.model.ReviewImage;

final class ReviewImageMapper {
    private ReviewImageMapper() {
    }

    static ReviewImageJpaEntity toEntity(ReviewImage reviewImage) {
        return ReviewImageJpaEntity.create(
            reviewImage.getReviewId() == null ? null : reviewImage.getReviewId().value(),
            reviewImage.getImageFileId() == null ? null : reviewImage.getImageFileId().value(),
            reviewImage.getSort()
        );
    }
}

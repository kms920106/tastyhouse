package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.review.model.ReviewImage;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ReviewImageMapper {
    private ReviewImageMapper() {
    }

    static ReviewImageJpaEntity toEntity(ReviewImage domain) {
        return ReviewImageJpaEntity.create(
            IdMapping.raw(domain.getReviewId(), ReviewId::value),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getSort()
        );
    }
}

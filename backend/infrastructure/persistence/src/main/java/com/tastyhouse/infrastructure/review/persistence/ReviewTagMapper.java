package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.TagId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ReviewTagMapper {
    private ReviewTagMapper() {
    }

    static ReviewTagJpaEntity toEntity(ReviewTag domain) {
        return ReviewTagJpaEntity.create(
            IdMapping.raw(domain.getReviewId(), ReviewId::value),
            IdMapping.raw(domain.getTagId(), TagId::value)
        );
    }
}

package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.TagId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 리뷰 태그 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ReviewTagMapper {

    private ReviewTagMapper() {
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ReviewTagJpaEntity toEntity(ReviewTag domain) {
        return ReviewTagJpaEntity.create(
            IdMapping.raw(domain.getReviewId(), ReviewId::value),
            IdMapping.raw(domain.getTagId(), TagId::value)
        );
    }
}

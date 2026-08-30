package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 리뷰 좋아요 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ReviewLikeMapper {

    private ReviewLikeMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ReviewLike toDomain(ReviewLikeJpaEntity entity) {
        return ReviewLike.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getReviewId(), ReviewId::of),
            IdMapping.vo(entity.getMemberId(), MemberId::of)
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ReviewLikeJpaEntity toEntity(ReviewLike domain) {
        return ReviewLikeJpaEntity.create(
            IdMapping.raw(domain.getReviewId(), ReviewId::value),
            IdMapping.raw(domain.getMemberId(), MemberId::value)
        );
    }
}

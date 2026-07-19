package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.core.domain.review.domain.model.ReviewComment;

/**
 * 리뷰 댓글 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ReviewCommentMapper {

    private ReviewCommentMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ReviewComment toDomain(ReviewCommentJpaEntity entity) {
        return ReviewComment.reconstitute(
            entity.getId(),
            entity.getReviewId(),
            entity.getMemberId(),
            entity.getContent(),
            entity.isHidden(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ReviewCommentJpaEntity toEntity(ReviewComment domain) {
        return ReviewCommentJpaEntity.create(
            domain.getReviewId(),
            domain.getMemberId(),
            domain.getContent(),
            domain.isHidden()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ReviewCommentJpaEntity entity, ReviewComment domain) {
        entity.applyChanges(domain.isHidden());
    }
}

package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 사장님 답변 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ReviewOwnerReplyMapper {

    private ReviewOwnerReplyMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ReviewOwnerReply toDomain(ReviewOwnerReplyJpaEntity entity) {
        return ReviewOwnerReply.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getReviewId(), ReviewId::of),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getCeoId(), CeoId::of),
            entity.getContent(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ReviewOwnerReplyJpaEntity toEntity(ReviewOwnerReply domain) {
        return ReviewOwnerReplyJpaEntity.create(
            IdMapping.raw(domain.getReviewId(), ReviewId::value),
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getCeoId(), CeoId::value),
            domain.getContent()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ReviewOwnerReplyJpaEntity entity, ReviewOwnerReply domain) {
        entity.applyChanges(domain.getContent());
    }
}

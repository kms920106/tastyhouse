package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopRequestComment;

/**
 * 요청건 문의 댓글 도메인 모델 ↔ JPA 엔티티 변환기. append-only라 update 경로
 * ({@code applyChanges})가 없다.
 */
final class ShopRequestCommentMapper {

    private ShopRequestCommentMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopRequestComment toDomain(ShopRequestCommentJpaEntity entity) {
        return ShopRequestComment.reconstitute(
            entity.getId(),
            entity.getShopRequestIndexId(),
            entity.getAuthorType(),
            entity.getAuthorId(),
            entity.getContent(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopRequestCommentJpaEntity toEntity(ShopRequestComment domain) {
        return ShopRequestCommentJpaEntity.create(
            domain.getShopRequestIndexId(),
            domain.getAuthorType(),
            domain.getAuthorId(),
            domain.getContent()
        );
    }
}

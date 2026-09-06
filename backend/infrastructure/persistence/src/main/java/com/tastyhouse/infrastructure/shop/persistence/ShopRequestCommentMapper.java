package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopRequestComment;

final class ShopRequestCommentMapper {
    private ShopRequestCommentMapper() {
    }

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

    static ShopRequestCommentJpaEntity toEntity(ShopRequestComment domain) {
        return ShopRequestCommentJpaEntity.create(
            domain.getShopRequestIndexId(),
            domain.getAuthorType(),
            domain.getAuthorId(),
            domain.getContent()
        );
    }
}

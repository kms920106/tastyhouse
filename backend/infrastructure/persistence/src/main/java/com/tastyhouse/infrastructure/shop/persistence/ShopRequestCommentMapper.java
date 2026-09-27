package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopRequestComment;
import com.tastyhouse.domain.shop.model.ShopRequestCommentAuthorType;

final class ShopRequestCommentMapper {
    private ShopRequestCommentMapper() {
    }

    static ShopRequestComment toDomain(ShopRequestCommentJpaEntity entity) {
        return ShopRequestComment.reconstitute(
            entity.getId(),
            entity.getShopRequestIndexId(),
            entity.getAuthorType() == null ? null : ShopRequestCommentAuthorType.valueOf(entity.getAuthorType()),
            entity.getAuthorId(),
            entity.getContent(),
            entity.getCreatedAt()
        );
    }

    static ShopRequestCommentJpaEntity toEntity(ShopRequestComment shopRequestComment) {
        return ShopRequestCommentJpaEntity.create(
            shopRequestComment.getShopRequestIndexId(),
            shopRequestComment.getAuthorType() == null ? null : shopRequestComment.getAuthorType().name(),
            shopRequestComment.getAuthorId(),
            shopRequestComment.getContent()
        );
    }
}

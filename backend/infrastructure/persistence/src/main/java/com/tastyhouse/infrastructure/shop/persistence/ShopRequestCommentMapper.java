package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopRequestCommentState;

final class ShopRequestCommentMapper {
    private ShopRequestCommentMapper() {
    }

    static ShopRequestCommentState toState(ShopRequestCommentJpaEntity entity) {
        return new ShopRequestCommentState(
            entity.getId(),
            entity.getShopRequestIndexId(),
            entity.getAuthorType(),
            entity.getAuthorId(),
            entity.getContent(),
            entity.getCreatedAt()
        );
    }

    static ShopRequestCommentJpaEntity toEntity(ShopRequestCommentState state) {
        return ShopRequestCommentJpaEntity.create(
            state.shopRequestIndexId(),
            state.authorType(),
            state.authorId(),
            state.content()
        );
    }
}

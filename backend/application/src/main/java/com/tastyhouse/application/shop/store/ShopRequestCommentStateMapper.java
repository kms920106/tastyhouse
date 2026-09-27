package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.ShopRequestComment;
import com.tastyhouse.domain.shop.model.ShopRequestCommentAuthorType;
import com.tastyhouse.application.shop.port.out.write.ShopRequestCommentState;

final class ShopRequestCommentStateMapper {
    private ShopRequestCommentStateMapper() {
    }

    static ShopRequestComment toDomain(ShopRequestCommentState state) {
        return ShopRequestComment.reconstitute(
            state.id(),
            state.shopRequestIndexId(),
            state.authorType() == null ? null : ShopRequestCommentAuthorType.valueOf(state.authorType()),
            state.authorId(),
            state.content(),
            state.createdAt()
        );
    }

    static ShopRequestCommentState toState(ShopRequestComment shopRequestComment) {
        return new ShopRequestCommentState(
            shopRequestComment.getId(),
            shopRequestComment.getShopRequestIndexId(),
            shopRequestComment.getAuthorType() == null ? null : shopRequestComment.getAuthorType().name(),
            shopRequestComment.getAuthorId(),
            shopRequestComment.getContent(),
            shopRequestComment.getCreatedAt()
        );
    }
}

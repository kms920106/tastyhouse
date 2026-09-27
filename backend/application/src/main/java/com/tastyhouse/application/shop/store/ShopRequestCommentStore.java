package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.ShopRequestComment;
import com.tastyhouse.application.shop.port.out.write.ShopRequestCommentStatePort;

public class ShopRequestCommentStore implements ShopRequestCommentRepository {
    private final ShopRequestCommentStatePort shopRequestCommentStatePort;

    public ShopRequestCommentStore(ShopRequestCommentStatePort shopRequestCommentStatePort) {
        this.shopRequestCommentStatePort = shopRequestCommentStatePort;
    }

    @Override
    public ShopRequestComment save(ShopRequestComment shopRequestComment) {
        return ShopRequestCommentStateMapper.toDomain(shopRequestCommentStatePort.save(ShopRequestCommentStateMapper.toState(shopRequestComment)));
    }
}

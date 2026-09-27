package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopRequestComment;

public interface ShopRequestCommentRepository {
    ShopRequestComment save(ShopRequestComment shopRequestComment);
}

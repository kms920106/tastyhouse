package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopRequestComment;

public interface ShopRequestCommentSavePort {

    ShopRequestComment save(ShopRequestComment shopRequestComment);
}

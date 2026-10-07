package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopNoticeResult;

public interface ShopNoticeQueryUseCase {

    ShopNoticeResult getShopNotice(Long shopId);
}

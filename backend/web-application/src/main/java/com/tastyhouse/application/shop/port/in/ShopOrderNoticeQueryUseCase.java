package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;

public interface ShopOrderNoticeQueryUseCase {

    ShopOrderNoticeResult getOrderNotice(Long shopId);
}

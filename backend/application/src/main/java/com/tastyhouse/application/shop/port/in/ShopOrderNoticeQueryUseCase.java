package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;

@WebApp
public interface ShopOrderNoticeQueryUseCase {

    ShopOrderNoticeResult getOrderNotice(Long shopId);
}

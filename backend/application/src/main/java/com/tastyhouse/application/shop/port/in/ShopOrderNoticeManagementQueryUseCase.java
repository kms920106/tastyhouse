package com.tastyhouse.application.shop.port.in;

import java.util.Optional;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;

@AdminApp
public interface ShopOrderNoticeManagementQueryUseCase {

    Optional<ShopOrderNoticeResult> getOrderNotice(Long shopId);
}

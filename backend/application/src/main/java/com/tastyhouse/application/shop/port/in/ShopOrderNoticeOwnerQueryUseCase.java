package com.tastyhouse.application.shop.port.in;

import java.util.Optional;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;

@CeoApp
public interface ShopOrderNoticeOwnerQueryUseCase {

    Optional<ShopOrderNoticeResult> getOrderNotice(Long ceoId, Long shopId);
}

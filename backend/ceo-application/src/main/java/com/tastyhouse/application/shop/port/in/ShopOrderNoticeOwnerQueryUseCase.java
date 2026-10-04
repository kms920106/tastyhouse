package com.tastyhouse.application.shop.port.in;

import java.util.Optional;

import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;

public interface ShopOrderNoticeOwnerQueryUseCase {

    Optional<ShopOrderNoticeResult> getOrderNotice(Long ceoId, Long shopId);
}

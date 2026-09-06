package com.tastyhouse.application.shop.port.out;

import java.util.Optional;

public interface ShopOrderNoticeQueryPort {

    Optional<ShopOrderNoticeResult> findVisibleOrderNotice(Long shopId);
}

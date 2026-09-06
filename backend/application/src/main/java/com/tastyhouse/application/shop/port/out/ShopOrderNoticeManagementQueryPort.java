package com.tastyhouse.application.shop.port.out;

import java.util.Optional;

public interface ShopOrderNoticeManagementQueryPort {

    Optional<ShopOrderNoticeResult> findOrderNotice(Long shopId);
}

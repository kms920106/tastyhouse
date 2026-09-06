package com.tastyhouse.application.shop.port.out;

import java.util.Optional;

public interface ShopNoticeQueryPort {

    Optional<ShopNoticeResult> findExposedNotice(Long shopId);
}

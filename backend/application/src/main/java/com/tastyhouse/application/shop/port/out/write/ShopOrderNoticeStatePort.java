package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

public interface ShopOrderNoticeStatePort {
    ShopOrderNoticeState save(ShopOrderNoticeState shopOrderNotice);

    Optional<ShopOrderNoticeState> findByShopId(Long shopId);
}

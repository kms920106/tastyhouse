package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

public interface ShopNoticeStatePort {
    ShopNoticeState save(ShopNoticeState shopNotice);

    Optional<ShopNoticeState> findById(Long id);

    Optional<ShopNoticeState> findExposedByShopId(Long shopId);

    void deleteById(Long id);
}

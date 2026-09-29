package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopNoticePersistencePort {
    ShopNotice save(ShopNotice shopNotice);

    Optional<ShopNotice> findById(Long id);

    Optional<ShopNotice> findExposedByShopId(ShopId shopId);

    void deleteById(Long id);
}

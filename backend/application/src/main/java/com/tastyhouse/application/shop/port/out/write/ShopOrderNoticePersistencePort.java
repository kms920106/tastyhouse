package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopOrderNoticePersistencePort {

    ShopOrderNotice save(ShopOrderNotice shopOrderNotice);

    Optional<ShopOrderNotice> findByShopId(ShopId shopId);
}

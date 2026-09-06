package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopOrderNoticeRepository {
    ShopOrderNotice save(ShopOrderNotice shopOrderNotice);

    Optional<ShopOrderNotice> findByShopId(ShopId shopId);
}

package com.tastyhouse.domain.shop.repository;

import java.util.Set;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopDeliveryTipRegionLookup {
    boolean existsRegionTipByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId);

    Set<AdminDongId> findRegionTipAdminDongIds(ShopId shopId);
}

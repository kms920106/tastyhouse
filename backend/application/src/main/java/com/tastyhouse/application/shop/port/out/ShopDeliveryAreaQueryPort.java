package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ShopDeliveryAreaQueryPort {

    List<ShopDeliveryAreaItemResult> findDeliveryAreas(Long shopId);

    Set<Long> findAdminDongIds(Long shopId);

    Set<Long> findAdminDongIdsBySource(Long shopId, String source);

    ShopLocationResult findShopLocation(Long ceoId, Long shopId);

    Optional<ShopDeliveryAreaPolygonResult> findPolygon(Long shopId);

    Set<Long> findRegionTipAdminDongIds(Long shopId);
}

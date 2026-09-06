package com.tastyhouse.domain.shop.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopDeliveryAreaRepository {
    List<ShopDeliveryArea> findByShopId(ShopId shopId);

    Optional<ShopDeliveryArea> findById(Long deliveryAreaId);

    boolean existsByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId);

    long countByShopId(ShopId shopId);

    ShopDeliveryArea save(ShopDeliveryArea shopDeliveryArea);

    List<ShopDeliveryArea> saveAll(List<ShopDeliveryArea> shopDeliveryAreas);

    List<ShopDeliveryArea> findByShopIdAndSource(ShopId shopId, DeliveryAreaSource source);

    void deleteByShopIdAndSource(ShopId shopId, DeliveryAreaSource source);

    Set<AdminDongId> findAdminDongIdsByShopId(ShopId shopId);

    void deleteById(Long deliveryAreaId);
}

package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ShopDeliveryAreaStatePort {
    List<ShopDeliveryAreaState> findByShopId(Long shopId);

    Optional<ShopDeliveryAreaState> findById(Long deliveryAreaId);

    boolean existsByShopIdAndAdminDongId(Long shopId, Long adminDongId);

    long countByShopId(Long shopId);

    ShopDeliveryAreaState save(ShopDeliveryAreaState shopDeliveryArea);

    List<ShopDeliveryAreaState> saveAll(List<ShopDeliveryAreaState> shopDeliveryAreas);

    List<ShopDeliveryAreaState> findByShopIdAndSource(Long shopId, String source);

    void deleteByShopIdAndSource(Long shopId, String source);

    Set<Long> findAdminDongIdsByShopId(Long shopId);

    void deleteById(Long deliveryAreaId);
}

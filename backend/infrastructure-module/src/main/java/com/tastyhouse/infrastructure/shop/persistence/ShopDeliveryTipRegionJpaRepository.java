package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopDeliveryTipRegionJpaRepository extends JpaRepository<ShopDeliveryTipRegionJpaEntity, Long> {

    List<ShopDeliveryTipRegionJpaEntity> findByShopId(Long shopId);

    long countByShopId(Long shopId);

    boolean existsByShopIdAndAdminDongId(Long shopId, Long adminDongId);

    void deleteByShopId(Long shopId);
}

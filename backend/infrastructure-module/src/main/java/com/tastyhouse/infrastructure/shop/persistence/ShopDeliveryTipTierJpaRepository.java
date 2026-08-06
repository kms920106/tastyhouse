package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopDeliveryTipTierJpaRepository extends JpaRepository<ShopDeliveryTipTierJpaEntity, Long> {

    List<ShopDeliveryTipTierJpaEntity> findByShopIdOrderByTierOrderAsc(Long shopId);

    void deleteByShopId(Long shopId);
}

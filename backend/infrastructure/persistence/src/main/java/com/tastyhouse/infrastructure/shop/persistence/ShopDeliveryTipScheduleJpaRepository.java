package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopDeliveryTipScheduleJpaRepository extends JpaRepository<ShopDeliveryTipScheduleJpaEntity, Long> {

    List<ShopDeliveryTipScheduleJpaEntity> findByShopId(Long shopId);

    void deleteByShopId(Long shopId);
}

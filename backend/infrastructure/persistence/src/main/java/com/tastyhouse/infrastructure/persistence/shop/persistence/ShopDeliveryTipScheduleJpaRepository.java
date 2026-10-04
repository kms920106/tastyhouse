package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface ShopDeliveryTipScheduleJpaRepository extends JpaRepository<ShopDeliveryTipScheduleJpaEntity, Long> {

    List<ShopDeliveryTipScheduleJpaEntity> findByShopId(Long shopId);

    void deleteByShopId(Long shopId);
}

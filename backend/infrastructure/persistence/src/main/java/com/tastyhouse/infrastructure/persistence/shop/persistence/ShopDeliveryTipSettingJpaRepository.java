package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ShopDeliveryTipSettingJpaRepository extends JpaRepository<ShopDeliveryTipSettingJpaEntity, Long> {

    Optional<ShopDeliveryTipSettingJpaEntity> findByShopId(Long shopId);
}

package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopDeliveryTipSettingJpaRepository extends JpaRepository<ShopDeliveryTipSettingJpaEntity, Long> {

    Optional<ShopDeliveryTipSettingJpaEntity> findByShopId(Long shopId);
}

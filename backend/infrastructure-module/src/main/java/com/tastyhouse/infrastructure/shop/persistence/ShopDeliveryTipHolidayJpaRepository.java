package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopDeliveryTipHolidayJpaRepository extends JpaRepository<ShopDeliveryTipHolidayJpaEntity, Long> {

    Optional<ShopDeliveryTipHolidayJpaEntity> findByShopId(Long shopId);

    void deleteByShopId(Long shopId);
}

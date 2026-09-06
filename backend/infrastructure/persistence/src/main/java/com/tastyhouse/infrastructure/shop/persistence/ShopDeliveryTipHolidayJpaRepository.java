package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopDeliveryTipHolidayJpaRepository extends JpaRepository<ShopDeliveryTipHolidayJpaEntity, Long> {
    Optional<ShopDeliveryTipHolidayJpaEntity> findByShopId(Long shopId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ShopDeliveryTipHolidayJpaEntity h where h.shopId = :shopId")
    void deleteByShopId(@Param("shopId") Long shopId);
}

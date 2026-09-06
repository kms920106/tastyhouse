package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopDeliveryTipTierJpaRepository extends JpaRepository<ShopDeliveryTipTierJpaEntity, Long> {
    List<ShopDeliveryTipTierJpaEntity> findByShopIdOrderByTierOrderAsc(Long shopId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ShopDeliveryTipTierJpaEntity t where t.shopId = :shopId")
    void deleteByShopId(@Param("shopId") Long shopId);
}

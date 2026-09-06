package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopDeliveryTipRegionJpaRepository extends JpaRepository<ShopDeliveryTipRegionJpaEntity, Long> {
    List<ShopDeliveryTipRegionJpaEntity> findByShopId(Long shopId);

    long countByShopId(Long shopId);

    boolean existsByShopIdAndAdminDongId(Long shopId, Long adminDongId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ShopDeliveryTipRegionJpaEntity r where r.shopId = :shopId")
    void deleteByShopId(@Param("shopId") Long shopId);
}

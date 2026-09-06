package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tastyhouse.domain.shop.model.DeliveryAreaSource;

public interface ShopDeliveryAreaJpaRepository extends JpaRepository<ShopDeliveryAreaJpaEntity, Long> {
    List<ShopDeliveryAreaJpaEntity> findByShopIdOrderByIdAsc(Long shopId);

    List<ShopDeliveryAreaJpaEntity> findByShopIdAndSource(Long shopId, DeliveryAreaSource source);

    boolean existsByShopIdAndAdminDongId(Long shopId, Long adminDongId);

    long countByShopId(Long shopId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ShopDeliveryAreaJpaEntity e WHERE e.shopId = :shopId AND e.source = :source")
    void deleteByShopIdAndSource(@Param("shopId") Long shopId, @Param("source") DeliveryAreaSource source);

    @Query("SELECT e.adminDongId FROM ShopDeliveryAreaJpaEntity e WHERE e.shopId = :shopId ORDER BY e.id ASC")
    List<Long> findAdminDongIdsByShopId(@Param("shopId") Long shopId);
}

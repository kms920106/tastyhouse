package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopDeliveryAreaJpaRepository extends JpaRepository<ShopDeliveryAreaJpaEntity, Long> {

    List<ShopDeliveryAreaJpaEntity> findByShopIdOrderByIdAsc(Long shopId);

    boolean existsByShopIdAndAdminDongId(Long shopId, Long adminDongId);

    long countByShopId(Long shopId);
}

package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopDeliveryAreaAdjustmentRequestJpaRepository extends JpaRepository<ShopDeliveryAreaAdjustmentRequestJpaEntity, Long> {

    boolean existsByShopIdAndStatusIn(Long shopId, List<String> statuses);
}

package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;

public interface ShopDeliveryAreaAdjustmentRequestJpaRepository extends JpaRepository<ShopDeliveryAreaAdjustmentRequestJpaEntity, Long> {

    boolean existsByShopIdAndStatusIn(Long shopId, List<DeliveryAreaAdjustmentStatus> statuses);
}

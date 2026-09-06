package com.tastyhouse.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopDeliveryAreaAdjustmentRequestRepository {
    Optional<ShopDeliveryAreaAdjustmentRequest> findById(Long id);

    boolean existsByShopIdAndStatusIn(ShopId shopId, List<DeliveryAreaAdjustmentStatus> statuses);

    ShopDeliveryAreaAdjustmentRequest save(ShopDeliveryAreaAdjustmentRequest request);
}

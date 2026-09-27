package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ShopDeliveryAreaAdjustmentRequestStatePort {
    Optional<ShopDeliveryAreaAdjustmentRequestState> findById(Long id);

    boolean existsByShopIdAndStatusIn(Long shopId, List<String> statuses);

    ShopDeliveryAreaAdjustmentRequestState save(ShopDeliveryAreaAdjustmentRequestState request);
}

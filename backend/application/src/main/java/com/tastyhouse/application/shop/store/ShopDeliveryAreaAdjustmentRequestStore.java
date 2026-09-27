package com.tastyhouse.application.shop.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaAdjustmentRequestStatePort;

public class ShopDeliveryAreaAdjustmentRequestStore implements ShopDeliveryAreaAdjustmentRequestRepository {
    private final ShopDeliveryAreaAdjustmentRequestStatePort shopDeliveryAreaAdjustmentRequestStatePort;

    public ShopDeliveryAreaAdjustmentRequestStore(ShopDeliveryAreaAdjustmentRequestStatePort shopDeliveryAreaAdjustmentRequestStatePort) {
        this.shopDeliveryAreaAdjustmentRequestStatePort = shopDeliveryAreaAdjustmentRequestStatePort;
    }

    @Override
    public Optional<ShopDeliveryAreaAdjustmentRequest> findById(Long id) {
        return shopDeliveryAreaAdjustmentRequestStatePort.findById(id).map(ShopDeliveryAreaAdjustmentRequestStateMapper::toDomain);
    }

    @Override
    public boolean existsByShopIdAndStatusIn(ShopId shopId, List<DeliveryAreaAdjustmentStatus> statuses) {
        return shopDeliveryAreaAdjustmentRequestStatePort.existsByShopIdAndStatusIn(shopId.value(), statuses.stream().map(DeliveryAreaAdjustmentStatus::name).toList());
    }

    @Override
    public ShopDeliveryAreaAdjustmentRequest save(ShopDeliveryAreaAdjustmentRequest request) {
        return ShopDeliveryAreaAdjustmentRequestStateMapper.toDomain(shopDeliveryAreaAdjustmentRequestStatePort.save(ShopDeliveryAreaAdjustmentRequestStateMapper.toState(request)));
    }
}

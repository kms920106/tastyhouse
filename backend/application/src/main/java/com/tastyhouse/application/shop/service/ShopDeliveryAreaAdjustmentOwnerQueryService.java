package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentOwnerQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentListItemResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopDeliveryAreaAdjustmentOwnerQueryService implements ShopDeliveryAreaAdjustmentOwnerQueryUseCase {

    private final ShopDeliveryAreaAdjustmentQueryPort shopDeliveryAreaAdjustmentQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaAdjustmentOwnerQueryService(
        ShopDeliveryAreaAdjustmentQueryPort shopDeliveryAreaAdjustmentQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopDeliveryAreaAdjustmentQueryPort = shopDeliveryAreaAdjustmentQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopDeliveryAreaAdjustmentListItemResult> getAdjustmentRequests(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopDeliveryAreaAdjustmentQueryPort.findAdjustmentRequests(shopId);
    }

}

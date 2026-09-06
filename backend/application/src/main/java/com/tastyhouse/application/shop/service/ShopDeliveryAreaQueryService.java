package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaItemResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopDeliveryAreaQueryService implements ShopDeliveryAreaQueryUseCase {

    private final ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaQueryService(ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopDeliveryAreaQueryPort = shopDeliveryAreaQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopDeliveryAreaItemResult> getDeliveryAreas(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopDeliveryAreaQueryPort.findDeliveryAreas(shopId);
    }

}

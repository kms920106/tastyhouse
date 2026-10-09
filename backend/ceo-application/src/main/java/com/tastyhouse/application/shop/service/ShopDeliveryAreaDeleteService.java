package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaLoadPort;

@Service
@Transactional
class ShopDeliveryAreaDeleteService implements ShopDeliveryAreaDeleteUseCase {

    private final ShopDeliveryAreaService shopDeliveryAreaService;
    private final ShopDeliveryAreaLoadPort shopDeliveryAreaLoadPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaDeleteService(
        ShopDeliveryAreaService shopDeliveryAreaService,
        ShopDeliveryAreaLoadPort shopDeliveryAreaLoadPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopDeliveryAreaService = shopDeliveryAreaService;
        this.shopDeliveryAreaLoadPort = shopDeliveryAreaLoadPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void removeDeliveryArea(ShopDeliveryAreaDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long deliveryAreaId = command.deliveryAreaId();

        ShopDeliveryArea deliveryArea = shopDeliveryAreaLoadPort.findById(deliveryAreaId)
            .orElseThrow(() -> new ResourceNotFoundException(CeoErrorCode.SHOP_DELIVERY_AREA_NOT_FOUND));
        shopOwnershipValidator.validateOwnership(ceoId, deliveryArea.getShopId().value());

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryAreaService.removeArea(deliveryAreaId, actor);
    }
}

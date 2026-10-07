package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopRequestCancelCommand;
import com.tastyhouse.application.shop.port.in.ShopRequestCancelUseCase;

@Service
@Transactional
class ShopRequestCancelService implements ShopRequestCancelUseCase {

    private final ShopRequestCancellationService shopRequestCancellationService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRequestCancelService(
        ShopRequestCancellationService shopRequestCancellationService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRequestCancellationService = shopRequestCancellationService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void cancelRequest(ShopRequestCancelCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long requestId = command.requestId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopRequestCancellationService.cancel(requestId, shopId);
    }
}

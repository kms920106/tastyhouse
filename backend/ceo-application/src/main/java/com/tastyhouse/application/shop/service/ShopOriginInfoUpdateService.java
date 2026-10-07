package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.OriginSourceType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopOriginInfoUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopOriginInfoUpdateUseCase;

@Service
@Transactional
class ShopOriginInfoUpdateService implements ShopOriginInfoUpdateUseCase {

    private final ShopOriginInfoService shopOriginInfoService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopOriginInfoUpdateService(
        ShopOriginInfoService shopOriginInfoService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopOriginInfoService = shopOriginInfoService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateOriginInfo(ShopOriginInfoUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String sourceType = command.sourceType();
        String content = command.content();
        String url = command.url();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopOriginInfoService.upsertOriginInfo(
            shopId,
            OriginSourceType.from(sourceType),
            content,
            url,
            ShopChangeActor.ceo(ceoId)
        );
    }
}

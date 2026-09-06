package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.service.ShopLifecycleService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopStatusCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopStatusUpdateCommand;

@Service
@CeoApp
@Transactional
public class ShopStatusCommandService implements ShopStatusCommandUseCase {

    private static final String STATUS_HIDDEN = "HIDDEN";

    private final ShopLifecycleService shopLifecycleService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopStatusCommandService(ShopLifecycleService shopLifecycleService, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopLifecycleService = shopLifecycleService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateStatus(ShopStatusUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String status = command.status();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        boolean hidden = STATUS_HIDDEN.equals(status);
        ShopId id = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopLifecycleService.changeVisibility(id, hidden, actor);
    }
}

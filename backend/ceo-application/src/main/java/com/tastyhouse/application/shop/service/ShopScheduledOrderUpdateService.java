package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopChangeValueFormatter;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopScheduledOrderUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopScheduledOrderUpdateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopSavePort;

@Service
@Transactional
class ShopScheduledOrderUpdateService implements ShopScheduledOrderUpdateUseCase {

    private final ShopSavePort shopSavePort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopScheduledOrderUpdateService(
        ShopSavePort shopSavePort,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopSavePort = shopSavePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public void updateScheduledOrder(ShopScheduledOrderUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        boolean enabled = command.enabled();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        String previousValue = describeScheduledOrder(shop.isScheduledOrderEnabled());

        shop.changeScheduledOrderEnabled(enabled);
        shopSavePort.save(shop);

        ShopId id = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopChangeHistoryRecorder.record(
            id,
            ShopChangeType.SCHEDULED_ORDER,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeScheduledOrder(shop.isScheduledOrderEnabled())
        );
    }

    private String describeScheduledOrder(boolean scheduledOrderEnabled) {
        return "예약주문 " + ShopChangeValueFormatter.enabled(scheduledOrderEnabled);
    }
}

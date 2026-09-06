package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.domain.shop.service.ShopChangeValueFormatter;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopScheduledOrderCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopScheduledOrderUpdateCommand;

@Service
@CeoApp
@Transactional
public class ShopScheduledOrderCommandService implements ShopScheduledOrderCommandUseCase {

    private final ShopRepository shopRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopScheduledOrderCommandService(
        ShopRepository shopRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopRepository = shopRepository;
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
        shopRepository.save(shop);

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

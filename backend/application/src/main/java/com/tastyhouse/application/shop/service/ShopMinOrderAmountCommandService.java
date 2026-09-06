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
import com.tastyhouse.application.shop.port.in.ShopMinOrderAmountCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopMinOrderAmountUpdateCommand;

@Service
@CeoApp
@Transactional
public class ShopMinOrderAmountCommandService implements ShopMinOrderAmountCommandUseCase {

    private final ShopRepository shopRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopMinOrderAmountCommandService(
        ShopRepository shopRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopRepository = shopRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public void updateMinOrderAmount(ShopMinOrderAmountUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        int minOrderAmount = command.minOrderAmount();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        String previousValue = describeMinOrderAmount(shop.getMinOrderAmount());

        shop.changeMinOrderAmount(minOrderAmount);
        shopRepository.save(shop);

        ShopId id = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopChangeHistoryRecorder.record(
            id,
            ShopChangeType.MIN_ORDER_AMOUNT,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeMinOrderAmount(shop.getMinOrderAmount())
        );
    }

    private String describeMinOrderAmount(int minOrderAmount) {
        return minOrderAmount == 0
            ? ShopChangeValueFormatter.unset()
            : ShopChangeValueFormatter.amount(minOrderAmount);
    }
}

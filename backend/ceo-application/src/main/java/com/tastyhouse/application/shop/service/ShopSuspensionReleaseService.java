package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopChangeValueFormatter;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopSuspensionReleaseCommand;
import com.tastyhouse.application.shop.port.in.ShopSuspensionReleaseUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopSuspensionPersistencePort;

@Service
@Transactional
class ShopSuspensionReleaseService implements ShopSuspensionReleaseUseCase {

    private final ShopSuspensionPersistencePort shopSuspensionPersistencePort;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopSuspensionReleaseService(
        ShopSuspensionPersistencePort shopSuspensionPersistencePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopSuspensionPersistencePort = shopSuspensionPersistencePort;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void releaseSuspension(ShopSuspensionReleaseCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long suspensionId = command.suspensionId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopSuspension shopSuspension = shopSuspensionPersistencePort.findById(suspensionId)
            .orElseThrow(() -> new ResourceNotFoundException(CeoErrorCode.SHOP_SUSPENSION_NOT_FOUND));
        if (!shopSuspension.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(CeoErrorCode.SHOP_SUSPENSION_NOT_FOUND);
        }

        String previousValue = describeSuspension(shopSuspension);
        shopSuspension.release(LocalDateTime.now());
        shopSuspensionPersistencePort.save(shopSuspension);

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopChangeHistoryRecorder.record(
            shopSuspension.getShopId(),
            ShopChangeType.ORDER_SUSPENSION,
            ShopChangeActionType.DELETE,
            actor,
            previousValue,
            null
        );
    }

    private String describeSuspension(ShopSuspension suspension) {
        String orderMethodLabel = suspension.getOrderMethod() == null
            ? "전체"
            : suspension.getOrderMethod().getDisplayName();
        return orderMethodLabel + " " + suspension.getReason().getDescription() + " "
            + ShopChangeValueFormatter.dateRange(
                suspension.getStartAt().toLocalDate(),
                suspension.getEndAt().toLocalDate()
            );
    }
}

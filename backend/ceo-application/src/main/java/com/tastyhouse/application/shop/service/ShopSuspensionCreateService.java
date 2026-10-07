package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopChangeValueFormatter;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.SuspensionReason;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopSuspensionCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopSuspensionCreateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopSuspensionPersistencePort;

@Service
@Transactional
class ShopSuspensionCreateService implements ShopSuspensionCreateUseCase {

    private final ShopSuspensionPersistencePort shopSuspensionPersistencePort;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopSuspensionCreateService(
        ShopSuspensionPersistencePort shopSuspensionPersistencePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopSuspensionPersistencePort = shopSuspensionPersistencePort;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<Long> createSuspension(ShopSuspensionCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String reason = command.reason();
        List<String> orderMethods = command.orderMethods();
        LocalDateTime startAt = command.startAt();
        LocalDateTime endAt = command.endAt();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        return createSuspensionsForShop(shopId, reason, orderMethods, startAt, endAt, actor);
    }

    private List<Long> createSuspensionsForShop(
        Long shopId,
        String reason,
        List<String> orderMethods,
        LocalDateTime startAt,
        LocalDateTime endAt,
        ShopChangeActor actor
    ) {
        SuspensionReason suspensionReason = SuspensionReason.from(reason);
        List<OrderMethod> targetOrderMethods = orderMethods == null || orderMethods.isEmpty()
            ? Collections.singletonList((OrderMethod) null)
            : orderMethods.stream().map(OrderMethod::from).toList();

        ShopId shopIdVo = ShopId.of(shopId);
        String previousValue = describeSuspensions(shopSuspensionPersistencePort.findByShopId(shopId));

        List<Long> createdIds = targetOrderMethods.stream()
            .map(orderMethod -> shopSuspensionPersistencePort
                .save(ShopSuspension.of(shopIdVo, suspensionReason, orderMethod, startAt, endAt))
                .getId())
            .toList();

        shopChangeHistoryRecorder.record(
            shopIdVo,
            ShopChangeType.ORDER_SUSPENSION,
            ShopChangeActionType.CREATE,
            actor,
            previousValue,
            describeSuspensions(shopSuspensionPersistencePort.findByShopId(shopId))
        );
        return createdIds;
    }

    private String describeSuspensions(List<ShopSuspension> suspensions) {
        List<String> lines = suspensions.stream()
            .filter(suspension -> suspension.getReleasedAt() == null)
            .map(this::describeSuspension)
            .toList();
        return ShopChangeValueFormatter.snapshot(lines);
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

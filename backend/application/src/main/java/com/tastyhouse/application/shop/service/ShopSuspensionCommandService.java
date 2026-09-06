package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.SuspensionReason;
import com.tastyhouse.domain.shop.repository.ShopSuspensionRepository;
import com.tastyhouse.domain.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.domain.shop.service.ShopChangeValueFormatter;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.shop.port.in.ShopSuspensionBulkCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopSuspensionCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopSuspensionCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopSuspensionReleaseCommand;

@Service
@CeoApp
@Transactional
public class ShopSuspensionCommandService implements ShopSuspensionCommandUseCase {

    private final ShopSuspensionRepository shopSuspensionRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopSuspensionCommandService(
        ShopSuspensionRepository shopSuspensionRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopSuspensionRepository = shopSuspensionRepository;
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

    @Override
    public void releaseSuspension(ShopSuspensionReleaseCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long suspensionId = command.suspensionId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopSuspension shopSuspension = shopSuspensionRepository.findById(suspensionId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_SUSPENSION_NOT_FOUND));
        if (!shopSuspension.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_SUSPENSION_NOT_FOUND);
        }

        String previousValue = describeSuspension(shopSuspension);
        shopSuspension.release(LocalDateTime.now());
        shopSuspensionRepository.save(shopSuspension);

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

    @Override
    public List<Long> createSuspensionsBulk(ShopSuspensionBulkCreateCommand command) {
        Long ceoId = command.ceoId();
        List<Long> shopIds = command.shopIds();
        String reason = command.reason();
        List<String> orderMethods = command.orderMethods();
        LocalDateTime startAt = command.startAt();
        LocalDateTime endAt = command.endAt();

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        return shopIds.stream()
            .peek(shopId -> shopOwnershipValidator.validateOwnership(ceoId, shopId))
            .flatMap(shopId -> createSuspensionsForShop(shopId, reason, orderMethods, startAt, endAt, actor).stream())
            .toList();
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
        String previousValue = describeSuspensions(shopSuspensionRepository.findByShopId(shopId));

        List<Long> createdIds = targetOrderMethods.stream()
            .map(orderMethod -> shopSuspensionRepository
                .save(ShopSuspension.of(shopIdVo, suspensionReason, orderMethod, startAt, endAt))
                .getId())
            .toList();

        shopChangeHistoryRecorder.record(
            shopIdVo,
            ShopChangeType.ORDER_SUSPENSION,
            ShopChangeActionType.CREATE,
            actor,
            previousValue,
            describeSuspensions(shopSuspensionRepository.findByShopId(shopId))
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

package com.tastyhouse.ceoapplication.shop.service;

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
import com.tastyhouse.ceoapplication.shop.port.in.ShopSuspensionBulkCreateCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopSuspensionCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopSuspensionCreateCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopSuspensionReleaseCommand;

/**
 * 점주용 가게 영업 임시중지 변경 서비스(CQRS command 측).
 *
 * <p>임시중지는 단일 애그리거트 연산이라 도메인 서비스로 하강하지 않고 write 포트로 직접 다룬다.
 * 주문유형을 여러 개 지정하면 유형별로 한 건씩 생성하며, 지정이 없으면 전체 주문유형 대상
 * 한 건(orderMethod = null)을 생성한다.
 *
 * <p><b>변경이력({@code ORDER_SUSPENSION})도 이 서비스가 기록한다</b> — 대응 도메인 서비스가 없어
 * 변경을 실제 수행하는 지점이 여기이기 때문이다. 한 번의 등록 요청이 주문유형 수만큼 여러 행을 만들 수
 * 있으므로, 이력은 <b>행마다가 아니라 요청(가게)마다 한 줄</b>로 남기고 그 가게의 임시중지 목록 스냅샷을
 * 변경 전후로 담는다 — 한 번의 저장 버튼이 이력 한 행이라는 규칙을 지키기 위한 것이다.
 */
@Service
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

    /**
     * 여러 가게에 같은 임시중지를 일괄 등록한다. 가게마다 소유권을 검증한다.
     */
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

    /**
     * 한 가게에 임시중지를 등록하고 이력 한 줄을 남긴다. 주문유형 수만큼 행이 생기더라도 이력은
     * 가게 목록 스냅샷 변경으로 한 줄만 남긴다.
     */
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

    /**
     * 임시중지 목록을 줄바꿈으로 결합한 스냅샷으로 요약한다. 해제된 건은 이미 끝난 중지라 현재 설정
     * 스냅샷에서 제외한다.
     */
    private String describeSuspensions(List<ShopSuspension> suspensions) {
        List<String> lines = suspensions.stream()
            .filter(suspension -> suspension.getReleasedAt() == null)
            .map(this::describeSuspension)
            .toList();
        return ShopChangeValueFormatter.snapshot(lines);
    }

    /**
     * 임시중지 1행을 한 줄로 요약한다(예: {@code "배달 가게사정 2026-08-11~2026-08-12"}). 주문유형을
     * 지정하지 않은 건은 전체 주문유형 대상이므로 "전체"로 표기한다.
     */
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

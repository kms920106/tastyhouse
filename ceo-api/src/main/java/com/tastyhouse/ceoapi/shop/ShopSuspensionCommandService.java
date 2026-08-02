package com.tastyhouse.ceoapi.shop;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.domain.shop.domain.model.ShopSuspension;
import com.tastyhouse.domain.shop.domain.model.SuspensionReason;
import com.tastyhouse.domain.shop.domain.repository.ShopSuspensionRepository;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 점주용 가게 영업 임시중지 변경 서비스(CQRS command 측).
 *
 * <p>임시중지는 단일 애그리거트 연산이라 도메인 서비스로 하강하지 않고 write 포트로 직접 다룬다.
 * 주문유형을 여러 개 지정하면 유형별로 한 건씩 생성하며, 지정이 없으면 전체 주문유형 대상
 * 한 건(orderMethod = null)을 생성한다.
 */
@Service
@Transactional
public class ShopSuspensionCommandService {

    private final ShopSuspensionRepository shopSuspensionRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopSuspensionCommandService(ShopSuspensionRepository shopSuspensionRepository, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopSuspensionRepository = shopSuspensionRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public List<Long> createSuspension(
        Long ceoId,
        Long shopId,
        String reason,
        List<String> orderMethods,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return createSuspensionsForShop(shopId, reason, orderMethods, startAt, endAt);
    }

    public void releaseSuspension(Long ceoId, Long shopId, Long suspensionId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopSuspension shopSuspension = shopSuspensionRepository.findById(suspensionId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_SUSPENSION_NOT_FOUND));
        if (!shopSuspension.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_SUSPENSION_NOT_FOUND);
        }

        shopSuspension.release(LocalDateTime.now());
        shopSuspensionRepository.save(shopSuspension);
    }

    /**
     * 여러 가게에 같은 임시중지를 일괄 등록한다. 가게마다 소유권을 검증한다.
     */
    public List<Long> createSuspensionsBulk(
        Long ceoId,
        List<Long> shopIds,
        String reason,
        List<String> orderMethods,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        return shopIds.stream()
            .peek(shopId -> shopOwnershipValidator.validateOwnership(ceoId, shopId))
            .flatMap(shopId -> createSuspensionsForShop(shopId, reason, orderMethods, startAt, endAt).stream())
            .toList();
    }

    private List<Long> createSuspensionsForShop(
        Long shopId,
        String reason,
        List<String> orderMethods,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        SuspensionReason suspensionReason = SuspensionReason.from(reason);
        List<OrderMethod> targetOrderMethods = orderMethods == null || orderMethods.isEmpty()
            ? Collections.singletonList((OrderMethod) null)
            : orderMethods.stream().map(OrderMethod::from).toList();

        ShopId shopIdVo = ShopId.of(shopId);
        return targetOrderMethods.stream()
            .map(orderMethod -> shopSuspensionRepository
                .save(ShopSuspension.of(shopIdVo, suspensionReason, orderMethod, startAt, endAt))
                .getId())
            .toList();
    }
}

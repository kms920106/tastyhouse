package com.tastyhouse.ceoapi.shop;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.SuspensionReason;
import com.tastyhouse.core.domain.shop.application.ShopSuspensionCommandService;
import com.tastyhouse.core.domain.shop.application.ShopSuspensionQueryService;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopSuspensionCreateCommand;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopSuspensionResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.ceoapi.shop.response.ShopSuspensionResponse;

/**
 * 점주용 가게 영업 임시중지 중개 서비스. 모든 조회·등록·해제는 로그인 점주(ceoId)의
 * 소유 가게로 한정하며, 소유권 검증은 {@link ShopOwnershipValidator}에 위임한다.
 */
@Service
@RequiredArgsConstructor
public class ShopSuspensionService {

    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopSuspensionCommandService shopSuspensionCommandService;
    private final ShopSuspensionQueryService shopSuspensionQueryService;

    public List<ShopSuspensionResponse> getSuspensions(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopSuspensionQueryService.findSuspensions(shopId).stream()
            .map(this::toShopSuspensionResponse)
            .toList();
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
        validateExists(shopId, suspensionId);
        shopSuspensionCommandService.releaseSuspension(suspensionId, LocalDateTime.now());
    }

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

        return targetOrderMethods.stream()
            .map(orderMethod -> {
                ShopSuspensionCreateCommand command = ShopSuspensionCreateCommand.of(shopId, suspensionReason, orderMethod, startAt, endAt);
                return shopSuspensionCommandService.createSuspension(command);
            })
            .toList();
    }

    private void validateExists(Long shopId, Long suspensionId) {
        boolean exists = shopSuspensionQueryService.findSuspensions(shopId).stream()
            .anyMatch(result -> result.id().equals(suspensionId));
        if (!exists) {
            throw new EntityNotFoundException(ErrorCode.SHOP_SUSPENSION_NOT_FOUND);
        }
    }

    private ShopSuspensionResponse toShopSuspensionResponse(ShopSuspensionResult dto) {
        return ShopSuspensionResponse.of(
            dto.id(),
            dto.shopId(),
            dto.reason().name(),
            dto.orderMethod() == null ? null : dto.orderMethod().name(),
            dto.startAt(),
            dto.endAt(),
            dto.releasedAt()
        );
    }
}

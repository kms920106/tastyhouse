package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopSuspensionQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;
import com.tastyhouse.application.shop.port.out.ShopSuspensionResult;
import com.tastyhouse.ceoapplication.shop.response.ShopSuspensionResponse;

/**
 * 점주용 가게 영업 임시중지 조회 서비스(CQRS query 측).
 */
@Service
@Transactional(readOnly = true)
public class ShopSuspensionQueryService implements ShopSuspensionQueryUseCase {

    private final ShopQueryPort shopQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopSuspensionQueryService(ShopQueryPort shopQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopQueryPort = shopQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopSuspensionResponse> getSuspensions(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopQueryPort.findSuspensions(shopId).stream()
            .map(this::toShopSuspensionResponse)
            .toList();
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

package com.tastyhouse.ceoapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopSuspensionResult;
import com.tastyhouse.ceoapi.shop.response.ShopSuspensionResponse;

/**
 * 점주용 가게 영업 임시중지 조회 서비스(CQRS query 측).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopSuspensionQueryService {

    private final ShopQueryDao shopQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public List<ShopSuspensionResponse> getSuspensions(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopQueryDao.findSuspensions(shopId).stream()
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

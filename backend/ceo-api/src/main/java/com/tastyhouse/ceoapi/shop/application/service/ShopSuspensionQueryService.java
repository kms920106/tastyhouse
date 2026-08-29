package com.tastyhouse.ceoapi.shop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopSuspensionResult;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopSuspensionResponse;

/**
 * 점주용 가게 영업 임시중지 조회 서비스(CQRS query 측).
 */
@Service
@Transactional(readOnly = true)
public class ShopSuspensionQueryService {

    private final ShopQueryDao shopQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopSuspensionQueryService(ShopQueryDao shopQueryDao, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopQueryDao = shopQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

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

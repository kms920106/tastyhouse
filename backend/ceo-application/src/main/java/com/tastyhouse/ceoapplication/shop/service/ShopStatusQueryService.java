package com.tastyhouse.ceoapplication.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopStatusQueryUseCase;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.application.shop.port.out.ShopStatusResult;

/**
 * 점주용 가게 노출 상태 조회 서비스(CQRS query 측).
 */
@Service
@Transactional(readOnly = true)
public class ShopStatusQueryService implements ShopStatusQueryUseCase {

    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopStatusQueryService(ShopOwnershipValidator shopOwnershipValidator) {
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopStatusResult getStatus(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return new ShopStatusResult(shop.isHidden(), shop.isPermanentlyClosed());
    }
}

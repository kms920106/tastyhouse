package com.tastyhouse.ceoapi.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.model.Shop;
import com.tastyhouse.ceoapi.shop.response.ShopStatusResponse;

/**
 * 점주용 가게 노출 상태 조회 서비스(CQRS query 측).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopStatusQueryService {

    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopStatusResponse getStatus(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return ShopStatusResponse.from(shop.isHidden(), shop.isPermanentlyClosed());
    }
}

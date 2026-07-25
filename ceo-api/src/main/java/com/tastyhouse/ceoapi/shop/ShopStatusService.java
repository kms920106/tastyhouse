package com.tastyhouse.ceoapi.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.domain.shop.application.ShopCommandService;
import com.tastyhouse.ceoapi.shop.response.ShopStatusResponse;

/**
 * 점주용 가게 노출 상태(노출정지) 관리 중개 서비스. 컨트롤러↔core 위임과 소유권 검증만 담당한다.
 */
@Service
@RequiredArgsConstructor
public class ShopStatusService {

    private final ShopCommandService shopCommandService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopStatusResponse getStatus(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return ShopStatusResponse.from(shop.isHidden(), shop.isPermanentlyClosed());
    }

    public void updateStatus(Long ceoId, Long shopId, String status) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        boolean hidden = "HIDDEN".equals(status);
        ShopId id = ShopId.of(shopId);
        shopCommandService.changeVisibility(id, hidden);
    }
}

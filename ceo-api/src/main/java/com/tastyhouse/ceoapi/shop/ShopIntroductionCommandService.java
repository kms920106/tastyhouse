package com.tastyhouse.ceoapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.service.ShopLifecycleService;

/**
 * 점주용 가게소개(사장님 한마디) 등록 서비스(CQRS command 측).
 *
 * <p>500자 제한·금칙어 검수 불변식은 도메인 서비스 {@link ShopLifecycleService}가 담당한다.
 */
@Service
@Transactional
public class ShopIntroductionCommandService {

    private final ShopLifecycleService shopLifecycleService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopIntroductionCommandService(ShopLifecycleService shopLifecycleService, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopLifecycleService = shopLifecycleService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public void updateIntroduction(Long ceoId, Long shopId, String message) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopLifecycleService.createOwnerMessage(shopId, message);
    }
}

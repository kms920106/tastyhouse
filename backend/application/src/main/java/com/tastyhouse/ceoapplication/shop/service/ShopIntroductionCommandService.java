package com.tastyhouse.ceoapplication.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.service.ShopLifecycleService;
import com.tastyhouse.ceoapplication.shop.port.in.ShopIntroductionCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopIntroductionUpdateCommand;

/**
 * 점주용 가게소개(사장님 한마디) 등록 서비스(CQRS command 측).
 *
 * <p>500자 제한·금칙어 검수 불변식과 변경이력({@code INTRODUCTION}) 기록은 도메인 서비스
 * {@link ShopLifecycleService}가 담당하고, 여기서는 소유권 검증과 변경 주체 전달만 책임진다.
 */
@Service
@Transactional
public class ShopIntroductionCommandService implements ShopIntroductionCommandUseCase {

    private final ShopLifecycleService shopLifecycleService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopIntroductionCommandService(ShopLifecycleService shopLifecycleService, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopLifecycleService = shopLifecycleService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateIntroduction(ShopIntroductionUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String message = command.message();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopLifecycleService.createOwnerMessage(shopId, message, ShopChangeActor.ceo(ceoId));
    }
}

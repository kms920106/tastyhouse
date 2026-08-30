package com.tastyhouse.ceoapplication.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.service.ShopLifecycleService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.ceoapplication.shop.port.in.ShopStatusCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopStatusUpdateCommand;

/**
 * 점주용 가게 노출 상태(노출정지) 변경 서비스(CQRS command 측).
 *
 * <p>진행 중 이미지 변경요청이 있으면 상태 변경을 차단하는 불변식은 도메인 서비스
 * {@link ShopLifecycleService}가 담당한다.
 *
 * <p><b>변경이력</b>: {@code SHOP_VISIBILITY} 기록은 변경 전 노출 상태를 추가 조회 없이 볼 수 있는
 * {@link ShopLifecycleService}가 담당하고, 이 서비스는 변경 주체({@link ShopChangeActor})만 만들어 전달한다.
 */
@Service
@Transactional
public class ShopStatusCommandService implements ShopStatusCommandUseCase {

    private static final String STATUS_HIDDEN = "HIDDEN";

    private final ShopLifecycleService shopLifecycleService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopStatusCommandService(ShopLifecycleService shopLifecycleService, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopLifecycleService = shopLifecycleService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateStatus(ShopStatusUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String status = command.status();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        boolean hidden = STATUS_HIDDEN.equals(status);
        ShopId id = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopLifecycleService.changeVisibility(id, hidden, actor);
    }
}

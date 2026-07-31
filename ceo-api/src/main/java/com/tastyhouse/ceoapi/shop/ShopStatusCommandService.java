package com.tastyhouse.ceoapi.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.service.ShopLifecycleService;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 점주용 가게 노출 상태(노출정지) 변경 서비스(CQRS command 측).
 *
 * <p>진행 중 이미지 변경요청이 있으면 상태 변경을 차단하는 불변식은 도메인 서비스
 * {@link ShopLifecycleService}가 담당한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ShopStatusCommandService {

    private static final String STATUS_HIDDEN = "HIDDEN";

    private final ShopLifecycleService shopLifecycleService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public void updateStatus(Long ceoId, Long shopId, String status) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        boolean hidden = STATUS_HIDDEN.equals(status);
        ShopId id = ShopId.of(shopId);
        shopLifecycleService.changeVisibility(id, hidden);
    }
}

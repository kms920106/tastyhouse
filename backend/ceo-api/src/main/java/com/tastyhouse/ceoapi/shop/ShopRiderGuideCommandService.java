package com.tastyhouse.ceoapi.shop;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.domain.shop.service.ShopRiderGuideService;

/**
 * 점주용 라이더 가게방문 안내 등록 서비스(CQRS command 측).
 *
 * <p>문구 등록 기준(금칙어·실주소 재기재·배차 특정)·좌표 범위·폐업 가게 차단 불변식은 도메인 서비스
 * {@link ShopRiderGuideService}가 담당하고, 여기서는 소유권 검증과 트랜잭션 경계만 책임진다.
 */
@Service
@Transactional
public class ShopRiderGuideCommandService {

    private final ShopRiderGuideService shopRiderGuideService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRiderGuideCommandService(
        ShopRiderGuideService shopRiderGuideService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRiderGuideService = shopRiderGuideService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 안내 문구를 등록·수정한다. 빈 값이면 문구를 삭제한다.
     */
    public void updateVisitGuide(Long ceoId, Long shopId, String visitGuide) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopRiderGuideService.updateVisitGuide(shopId, visitGuide, RiderGuideActorType.CEO, ceoId);
    }

    public void updatePickupLocation(
        Long ceoId,
        Long shopId,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopRiderGuideService.updatePickupLocation(shopId, roadAddress, lotAddress, detailAddress, latitude, longitude);
    }

    /**
     * 픽업 위치를 비워 가게 실주소로 폴백시킨다. 이미 미설정 상태에서 호출해도 정상 처리된다(멱등).
     */
    public void clearPickupLocation(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopRiderGuideService.clearPickupLocation(shopId);
    }
}

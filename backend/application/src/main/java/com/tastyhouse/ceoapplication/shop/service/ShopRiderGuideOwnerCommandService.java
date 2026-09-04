package com.tastyhouse.ceoapplication.shop.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.domain.shop.service.ShopRiderGuideService;
import com.tastyhouse.ceoapplication.shop.port.in.ShopRiderGuideOwnerCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopRiderPickupLocationClearCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopRiderPickupLocationOwnerUpdateCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopRiderVisitGuideUpdateCommand;

/**
 * 점주용 라이더 가게방문 안내 등록 서비스(CQRS command 측).
 *
 * <p>문구 등록 기준(금칙어·실주소 재기재·배차 특정)·좌표 범위·폐업 가게 차단 불변식은 도메인 서비스
 * {@link ShopRiderGuideService}가 담당하고, 여기서는 소유권 검증과 트랜잭션 경계만 책임진다.
 */
@Service
@Transactional
public class ShopRiderGuideOwnerCommandService implements ShopRiderGuideOwnerCommandUseCase {

    private final ShopRiderGuideService shopRiderGuideService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRiderGuideOwnerCommandService(
        ShopRiderGuideService shopRiderGuideService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRiderGuideService = shopRiderGuideService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 안내 문구를 등록·수정한다. 빈 값이면 문구를 삭제한다.
     */
    @Override
    public void updateVisitGuide(ShopRiderVisitGuideUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String visitGuide = command.visitGuide();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopRiderGuideService.updateVisitGuide(shopId, visitGuide, RiderGuideActorType.CEO, ceoId);
    }

    @Override
    public void updatePickupLocation(ShopRiderPickupLocationOwnerUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String roadAddress = command.roadAddress();
        String lotAddress = command.lotAddress();
        String detailAddress = command.detailAddress();
        BigDecimal latitude = command.latitude();
        BigDecimal longitude = command.longitude();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopRiderGuideService.updatePickupLocation(
            shopId, roadAddress, lotAddress, detailAddress, latitude, longitude, RiderGuideActorType.CEO, ceoId
        );
    }

    /**
     * 픽업 위치를 비워 가게 실주소로 폴백시킨다. 이미 미설정 상태에서 호출해도 정상 처리된다(멱등).
     */
    @Override
    public void clearPickupLocation(ShopRiderPickupLocationClearCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopRiderGuideService.clearPickupLocation(shopId, RiderGuideActorType.CEO, ceoId);
    }
}

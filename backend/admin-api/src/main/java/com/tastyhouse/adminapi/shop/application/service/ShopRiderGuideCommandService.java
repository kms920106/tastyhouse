package com.tastyhouse.adminapi.shop.application.service;

import com.tastyhouse.adminapi.shop.application.port.in.ShopRiderGuideCommandUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopRiderPickupLocationUpdateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopRiderVisitGuideDeleteCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopRiderVisitGuideRevisionCommand;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.domain.shop.service.ShopRiderGuideService;

/**
 * admin용 라이더 안내 검수 조치 서비스(CQRS command 측).
 *
 * <p>라이더 안내는 승인 워크플로가 아니라 <b>등록 즉시 반영 + 관리자 사후 검수</b> 모델이므로, 관리자
 * 조치는 사전 승인·반려가 아니라 수정 요청과 삭제다. 관리자는 소유권 검증 없이 모든 가게를 조치할 수
 * 있다(admin 무제한 원칙).
 */
@Service
@Transactional
public class ShopRiderGuideCommandService implements ShopRiderGuideCommandUseCase {

    private final ShopRiderGuideService shopRiderGuideService;

    public ShopRiderGuideCommandService(ShopRiderGuideService shopRiderGuideService) {
        this.shopRiderGuideService = shopRiderGuideService;
    }

    /**
     * 부적합 문구를 삭제하고 사유와 함께 이력을 남긴다. 픽업 위치는 건드리지 않는다.
     */
    @Override
    public void deleteVisitGuide(ShopRiderVisitGuideDeleteCommand command) {
        Long shopId = command.shopId();
        Long adminId = command.adminId();
        String reason = command.reason();

        shopRiderGuideService.deleteVisitGuide(shopId, adminId, reason);
    }

    /**
     * 문구는 그대로 두고 수정 요청 이력만 남긴다.
     *
     * @return 생성된 이력 ID
     */
    @Override
    public Long requestRevision(ShopRiderVisitGuideRevisionCommand command) {
        Long shopId = command.shopId();
        Long adminId = command.adminId();
        String reason = command.reason();

        return shopRiderGuideService.requestRevision(shopId, adminId, reason);
    }

    /**
     * 라이더 제보를 반영해 픽업 위치를 교정한다.
     *
     * <p>관리자 교정은 점주가 한 변경이 아니므로 가게 변경이력({@code RIDER_PICKUP_LOCATION})에 남지
     * 않는다 — 액터를 넘기는 것은 도메인 서비스가 그 판정을 하기 위함이다.
     */
    @Override
    public void updatePickupLocation(ShopRiderPickupLocationUpdateCommand command) {
        Long shopId = command.shopId();
        Long adminId = command.adminId();
        String roadAddress = command.roadAddress();
        String lotAddress = command.lotAddress();
        String detailAddress = command.detailAddress();
        BigDecimal latitude = command.latitude();
        BigDecimal longitude = command.longitude();

        shopRiderGuideService.updatePickupLocation(
            shopId, roadAddress, lotAddress, detailAddress, latitude, longitude, RiderGuideActorType.ADMIN, adminId
        );
    }
}

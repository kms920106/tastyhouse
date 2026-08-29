package com.tastyhouse.ceoapi.shop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopBusinessHourQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;
import com.tastyhouse.apicommon.shop.response.ShopBreakTimeResponse;
import com.tastyhouse.apicommon.shop.response.ShopBusinessHourResponse;

/**
 * 점주용 영업시간·휴게시간 조회 서비스(CQRS query 측).
 *
 * <p>영업시간·휴게시간 모두 표현 목적 조회이므로 infra query DAO에서 Result를 받아 조립한다 — 같은
 * 데이터를 도메인 서비스도 불변식 검증·영업 상태 판정에 쓰지만, 그쪽은 write 포트로 도메인 모델을
 * 로드하므로 목적과 반환 타입이 달라 중복이 아니다. 모든 조회는 로그인 점주(ceoId)의 소유 가게로
 * 한정하며, 소유권 검증은 {@link ShopOwnershipValidator}에 위임한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopBusinessHourQueryService implements ShopBusinessHourQueryUseCase {

    private final ShopQueryPort shopQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopBusinessHourQueryService(ShopQueryPort shopQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopQueryPort = shopQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopBusinessHourResponse> getBusinessHours(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopQueryPort.findBusinessHours(shopId).stream()
            .map(this::toShopBusinessHourResponse)
            .toList();
    }

    @Override
    public List<ShopBreakTimeResponse> getBreakTimes(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopQueryPort.findBreakTimes(shopId).stream()
            .map(this::toShopBreakTimeResponse)
            .toList();
    }

    private ShopBusinessHourResponse toShopBusinessHourResponse(ShopBusinessHourResult businessHour) {
        return ShopBusinessHourResponse.from(
            businessHour.id(),
            businessHour.dayType().name(),
            businessHour.dayType().getDescription(),
            businessHour.openTime(),
            businessHour.closeTime(),
            businessHour.closed(),
            businessHour.allDay()
        );
    }

    private ShopBreakTimeResponse toShopBreakTimeResponse(ShopBreakTimeResult breakTime) {
        return ShopBreakTimeResponse.from(
            breakTime.id(),
            breakTime.dayType().name(),
            breakTime.dayType().getDescription(),
            breakTime.startTime(),
            breakTime.endTime()
        );
    }
}

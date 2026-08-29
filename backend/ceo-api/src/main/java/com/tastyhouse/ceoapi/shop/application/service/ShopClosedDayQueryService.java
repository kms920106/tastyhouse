package com.tastyhouse.ceoapi.shop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.shop.application.port.in.ShopClosedDayQueryUseCase;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;
import com.tastyhouse.application.shop.port.out.ShopTemporaryClosureResult;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopClosedDaysResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopRegularClosedDayResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopTemporaryClosureResponse;

/**
 * 점주용 휴무(공휴일 토글·정기 휴무·임시 휴무) 조회 서비스(CQRS query 측).
 *
 * <p>공휴일 휴무 여부는 소유권 검증이 반환한 가게 도메인에서, 정기휴무·임시휴무는 infra query DAO에서
 * 각각 얻어 함께 조립한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopClosedDayQueryService implements ShopClosedDayQueryUseCase {

    private final ShopQueryPort shopQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopClosedDayQueryService(ShopQueryPort shopQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopQueryPort = shopQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopClosedDaysResponse getClosedDays(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<ShopRegularClosedDayResponse> regularClosedDays = shopQueryPort.findClosedDays(shopId).stream()
            .map(this::toShopRegularClosedDayResponse)
            .toList();
        List<ShopTemporaryClosureResponse> temporaryClosures = shopQueryPort.findTemporaryClosures(shopId).stream()
            .map(this::toShopTemporaryClosureResponse)
            .toList();

        return ShopClosedDaysResponse.from(shop.isClosedOnPublicHolidays(), regularClosedDays, temporaryClosures);
    }

    private ShopRegularClosedDayResponse toShopRegularClosedDayResponse(ShopClosedDayResult closedDay) {
        return ShopRegularClosedDayResponse.from(
            closedDay.id(),
            closedDay.closedDayType().name(),
            closedDay.closedDayType().getDescription()
        );
    }

    private ShopTemporaryClosureResponse toShopTemporaryClosureResponse(ShopTemporaryClosureResult dto) {
        return ShopTemporaryClosureResponse.from(
            dto.id(),
            dto.startDate(),
            dto.endDate()
        );
    }
}

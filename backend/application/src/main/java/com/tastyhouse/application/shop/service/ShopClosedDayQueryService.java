package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopClosedDayQueryUseCase;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOwnerQueryPort;
import com.tastyhouse.application.shop.port.out.ShopTemporaryClosureResult;
import com.tastyhouse.application.shop.port.out.ShopClosedDaysResult;

/**
 * 점주용 휴무(공휴일 토글·정기 휴무·임시 휴무) 조회 서비스(CQRS query 측).
 *
 * <p>공휴일 휴무 여부는 소유권 검증이 반환한 가게 도메인에서, 정기휴무·임시휴무는 infra query DAO에서
 * 각각 얻어 함께 조립한다.
 */
@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopClosedDayQueryService implements ShopClosedDayQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnerQueryPort shopOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopClosedDayQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort, ShopOwnerQueryPort shopOwnerQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnerQueryPort = shopOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopClosedDaysResult getClosedDays(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<ShopClosedDayResult> regularClosedDays = shopBasicInfoQueryPort.findClosedDays(shopId);
        List<ShopTemporaryClosureResult> temporaryClosures = shopOwnerQueryPort.findTemporaryClosures(shopId);

        return new ShopClosedDaysResult(shop.isClosedOnPublicHolidays(), regularClosedDays, temporaryClosures);
    }
}

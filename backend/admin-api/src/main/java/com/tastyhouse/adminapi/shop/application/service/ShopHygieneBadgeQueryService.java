package com.tastyhouse.adminapi.shop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.out.ShopHygieneBadgeResult;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;
import com.tastyhouse.apicommon.shop.response.ShopHygieneBadgeResponse;
import com.tastyhouse.adminapi.shop.application.port.in.ShopHygieneBadgeQueryUseCase;

/**
 * admin용 가게 위생 인증 뱃지 조회 서비스(CQRS query 측). 소유권 검증 없이 전체 가게를 대상으로 한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopHygieneBadgeQueryService implements ShopHygieneBadgeQueryUseCase {

    private final ShopQueryPort shopQueryPort;

    public ShopHygieneBadgeQueryService(ShopQueryPort shopQueryPort) {
        this.shopQueryPort = shopQueryPort;
    }

    @Override
    public List<ShopHygieneBadgeResponse> getHygieneBadges(Long shopId) {
        return shopQueryPort.findHygieneBadges(shopId).stream()
            .map(this::toShopHygieneBadgeResponse)
            .toList();
    }

    private ShopHygieneBadgeResponse toShopHygieneBadgeResponse(ShopHygieneBadgeResult dto) {
        return ShopHygieneBadgeResponse.of(
            dto.id(),
            dto.shopId(),
            dto.badgeType().name(),
            dto.certifiedDate(),
            dto.lastInspectionMonth()
        );
    }
}

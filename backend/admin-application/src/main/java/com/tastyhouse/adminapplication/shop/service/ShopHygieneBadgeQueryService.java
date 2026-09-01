package com.tastyhouse.adminapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.out.ShopHygieneBadgeResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.adminapplication.shop.port.in.ShopHygieneBadgeQueryUseCase;
import com.tastyhouse.adminapplication.shop.response.ShopHygieneBadgeResponse;

/**
 * admin용 가게 위생 인증 뱃지 조회 서비스(CQRS query 측). 소유권 검증 없이 전체 가게를 대상으로 한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopHygieneBadgeQueryService implements ShopHygieneBadgeQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopHygieneBadgeQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public List<ShopHygieneBadgeResponse> getHygieneBadges(Long shopId) {
        return shopBasicInfoQueryPort.findHygieneBadges(shopId).stream()
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

package com.tastyhouse.adminapi.shop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.shop.query.ShopHygieneBadgeResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.apicommon.shop.response.ShopHygieneBadgeResponse;

/**
 * admin용 가게 위생 인증 뱃지 조회 서비스(CQRS query 측). 소유권 검증 없이 전체 가게를 대상으로 한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopHygieneBadgeQueryService {

    private final ShopQueryDao shopQueryDao;

    public ShopHygieneBadgeQueryService(ShopQueryDao shopQueryDao) {
        this.shopQueryDao = shopQueryDao;
    }

    public List<ShopHygieneBadgeResponse> getHygieneBadges(Long shopId) {
        return shopQueryDao.findHygieneBadges(shopId).stream()
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

package com.tastyhouse.adminapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.infrastructure.shop.query.ShopHygieneBadgeResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.adminapi.shop.response.ShopHygieneBadgeResponse;

/**
 * admin용 가게 위생 인증 뱃지 조회 서비스(CQRS query 측). 소유권 검증 없이 전체 가게를 대상으로 한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopHygieneBadgeQueryService {

    private final ShopQueryDao shopQueryDao;

    public List<ShopHygieneBadgeResponse> getHygieneBadges(Long shopId) {
        return shopQueryDao.findHygieneBadges(shopId).stream()
            .map(this::toShopHygieneBadgeResponse)
            .toList();
    }

    /**
     * 뱃지 등록 응답 — 명령이 돌려준 식별자로 커밋 이후 재조회해 조립한다
     * ({@link ShopHygieneBadgeCommandService}가 식별자만 반환하므로).
     */
    public ShopHygieneBadgeResponse getHygieneBadge(Long hygieneBadgeId) {
        ShopHygieneBadgeResult badge = shopQueryDao.findHygieneBadge(hygieneBadgeId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_HYGIENE_BADGE_NOT_FOUND));

        return toShopHygieneBadgeResponse(badge);
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

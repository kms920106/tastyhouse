package com.tastyhouse.ceoapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.shop.application.ShopHygieneBadgeQueryService;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopHygieneBadgeResult;
import com.tastyhouse.ceoapi.shop.response.ShopHygieneBadgeResponse;

/**
 * 점주용 가게 위생 인증 뱃지 조회 전용 중개 서비스. 등록/삭제는 admin 전용이라 여기서는 조회만 제공한다.
 * 모든 조회는 로그인 점주(ceoId)의 소유 가게로 한정하며, 소유권 검증은 {@link ShopOwnershipValidator}에 위임한다.
 */
@Service
@RequiredArgsConstructor
public class ShopHygieneBadgeService {

    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopHygieneBadgeQueryService shopHygieneBadgeQueryService;

    public List<ShopHygieneBadgeResponse> getHygieneBadges(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopHygieneBadgeQueryService.findByShopId(shopId).stream()
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

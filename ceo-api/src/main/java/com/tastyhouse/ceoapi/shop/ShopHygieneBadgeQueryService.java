package com.tastyhouse.ceoapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.shop.query.ShopHygieneBadgeResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.apicommon.shop.response.ShopHygieneBadgeResponse;

/**
 * 점주용 가게 위생 인증 뱃지 조회 서비스(CQRS query 측).
 *
 * <p>등록/삭제는 admin 전용이라 ceo-api에는 command 서비스를 두지 않는다. 모든 조회는 로그인
 * 점주(ceoId)의 소유 가게로 한정하며, 소유권 검증은 {@link ShopOwnershipValidator}에 위임한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopHygieneBadgeQueryService {

    private final ShopQueryDao shopQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public List<ShopHygieneBadgeResponse> getHygieneBadges(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
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
